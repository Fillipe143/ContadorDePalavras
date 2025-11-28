import org.jocl.*;
import static org.jocl.CL.*;

public class JOCLGPUSearcher implements ISearcher {

    private static final String KERNEL_CODE =
            "__kernel void searchKernel(__global const char* text, " +
                    "                           const int textLength, " +
                    "                           __global const char* pattern, " +
                    "                           const int patternLength, " +
                    "                           __global int* result) { " +
                    "    int gid = get_global_id(0); " +
                    "    if (gid > textLength - patternLength) return; " +
                    "    " +
                    "    bool match = true; " +
                    "    for (int i = 0; i < patternLength; i++) { " +
                    "        if (text[gid + i] != pattern[i]) { " +
                    "            match = false; " +
                    "            break; " +
                    "        } " +
                    "    } " +
                    "    " +
                    "    if (match) { " +
                    "        atomic_inc(result); " +
                    "    } " +
                    "}";

    private cl_context context;
    private cl_command_queue commandQueue;
    private cl_program program;
    private cl_kernel kernel;

    public JOCLGPUSearcher() {
        initOpenCL();
    }

    private void initOpenCL() {
        CL.setExceptionsEnabled(true);

        int platformIndex = 0;
        long deviceType = CL_DEVICE_TYPE_GPU;

        int[] numPlatformsArray = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];
        cl_platform_id[] platforms = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        int[] numDevicesArray = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];
        cl_device_id[] devices = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[0]; // Pega a primeira GPU

        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        context = clCreateContext(contextProperties, 1, new cl_device_id[]{device}, null, null, null);

        commandQueue = clCreateCommandQueueWithProperties(context, device, null, null);

        program = clCreateProgramWithSource(context, 1, new String[]{KERNEL_CODE}, null, null);
        clBuildProgram(program, 0, null, null, null, null);

        kernel = clCreateKernel(program, "searchKernel", null);
    }

    @Override
    public long search(byte[] text, byte[] word) {
        int n = text.length;
        int m = word.length;
        int[] result = new int[]{0};

        cl_mem memText = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_char * n, Pointer.to(text), null);
        cl_mem memWord = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_char * m, Pointer.to(word), null);
        cl_mem memResult = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_int, Pointer.to(result), null);

        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memText));
        clSetKernelArg(kernel, 1, Sizeof.cl_int, Pointer.to(new int[]{n}));
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memWord));
        clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{m}));
        clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(memResult));

        long[] globalWorkSize = new long[]{n};

        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, globalWorkSize, null, 0, null, null);

        clEnqueueReadBuffer(commandQueue, memResult, CL_TRUE, 0, Sizeof.cl_int, Pointer.to(result), 0, null, null);

        clReleaseMemObject(memText);
        clReleaseMemObject(memWord);
        clReleaseMemObject(memResult);

        return result[0];
    }

    public void cleanup() {
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
    }

    @Override
    public String getName() {
        return "ParallelGPU";
    }
}