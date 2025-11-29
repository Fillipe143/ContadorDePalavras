# **Análise comparativa de algoritmos com uso de paralelismo**

Alunos:
- Fillipe - 2315058
- Kalil - 2223857

## **Resumo**
Este trabalho apresenta uma análise comparativa do desempenho de algoritmos de busca e contagem de palavras executados de forma serial e paralela em Java. Foram implementadas três versões do algoritmo: uma execução serial na CPU, uma execução paralela na CPU utilizando 1, 2, 4, 8 e 16 threads, e uma execução paralela na GPU empregando OpenCL por meio da biblioteca JOCL. Para avaliar o impacto do paralelismo, foram utilizados textos com diferentes tamanhos (10%, 50% e 100% de cada livro), permitindo observar o comportamento dos algoritmos sob variação de carga de processamento. Os tempos de execução foram registrados em arquivos CSV e analisados por meio de gráficos gerados com JFreeChart. Os resultados obtidos evidenciam as diferenças de desempenho entre as abordagens, destacando os ganhos de velocidade obtidos com o aumento de threads na CPU e os desafios associados ao uso de GPU para tarefas baseadas em manipulação de texto. O estudo contribui para a compreensão prática da eficiência de técnicas de paralelismo em ambientes multicore e GPU, oferecendo subsídios para decisões de otimização em aplicações que lidam com grandes volumes de dados textuais.

## Introdução 
O aumento do volume de dados processados por aplicações modernas tem intensificado a necessidade de algoritmos mais eficientes, especialmente em tarefas como a busca e contagem de palavras em grandes textos. Nesse cenário, o uso de paralelismo em arquiteturas multicore e GPU surge como uma alternativa relevante para acelerar processos tradicionalmente executados de forma sequencial.
Este trabalho realiza uma análise comparativa entre três abordagens de contagem de palavras em Java: uma versão serial na CPU, uma versão paralela em CPU utilizando diferentes quantidades de threads (1, 2, 4, 8 e 16), e uma versão paralela em GPU utilizando OpenCL por meio da biblioteca JOCL. Para avaliar o impacto do paralelismo, foram utilizados textos de tamanhos variados, considerando 10%, 50% e 100% de cada obra, permitindo observar como o desempenho se comporta conforme o volume de dados cresce.
Os tempos de execução obtidos foram registrados em arquivos CSV e posteriormente analisados visualmente por meio de gráficos gerados com JFreeChart, possibilitando comparar eficiência, escalabilidade e comportamento dos algoritmos nos diferentes cenários testados.
Assim, esta introdução contextualiza a importância da análise de desempenho entre métodos seriais e paralelos e apresenta a abordagem adotada no estudo, que busca identificar como CPU e GPU respondem às variações no tamanho dos dados e no grau de paralelismo empregado.

## **Metodologia**
A metodologia adotada para construção deste trabalho foi estruturada em etapas que envolvem desenvolvimento, execução e análise de desempenho de algoritmos de busca e contagem de palavras em suas versões sequenciais e paralelas, incluindo implementação em GPU.
Algoritmos implementados: Serial CPU Searcher, Parallel CPU Searcher e Parallel GPU Searcher. Cada algoritmo foi desenvolvido em diferentes versões:
Sequencial: execução linear utilizando estruturas tradicionais da linguagem Java com loops aninhados.
Paralela em CPU: utilizando múltiplas threads através do ForkJoinPool e Streams paralelos do Java.
Paralela em GPU: utilizando processamento massivamente paralelo através da biblioteca JOCL com OpenCL.
As versões paralelas foram estruturadas utilizando recursos de concorrência do Java, como ForkJoinPool para gerenciamento automático de threads, divisão do trabalho de busca em múltiplas posições do texto para execução simultânea e sincronização dos resultados através de operações atômicas no caso da GPU. A versão GPU utilizou kernels OpenCL para criar threads dinamicamente conforme o tamanho do texto processado.
Para a estrutura do framework de teste foram desenvolvidas várias classes: SerialCPUSearcher e ParallelCPUSearcher para as implementações CPU, JOCLGPUSearcher para a implementação em GPU, Main responsável pela execução dos testes e criação do arquivo CSV, e ChartGenerator responsável pela geração dos gráficos utilizando JFreeChart a partir do arquivo CSV.
Textos de entrada: três livros de domínio público (Don Quixote, Dracula, Moby Dick) com diferentes tamanhos.
Amostras de tamanho: 10%, 50% e 100% do conteúdo total de cada livro.
Configuração de threads: 1, 2, 4, 8 e 16 threads para as versões paralelas em CPU.
Palavra de busca: "the" como termo fixo para todos os experimentos.
Chamada automatizada de cada algoritmo sequencial e paralelo, registrando o tempo de execução com System.nanoTime().
A execução dos experimentos foi realizada utilizando as diferentes amostras de texto (10%, 50%, 100%) com número de threads de 1, 2, 4, 8 e 16, sendo 1 thread utilizada para o algoritmo sequencial e também como base de comparação para as versões paralelas. O ambiente de execução foi multicore, explorando a capacidade de paralelização da CPU, com adicional de processamento em GPU através da biblioteca JOCL.
Para cada execução foram registrados: nome do arquivo, tamanho em bytes, nome do algoritmo, quantidade de threads utilizadas, número de ocorrências encontradas e tempo de execução em milissegundos.
Os resultados foram armazenados em um arquivo CSV gerado pelo programa, facilitando a análise dos dados coletados por meio de comparação de tempos entre abordagens serial e paralela, avaliação de ganho de desempenho em função do número de threads, análise de escalabilidade conforme aumento do tamanho dos dados e observação do comportamento da GPU em tarefas de manipulação textual. Com isso, foi possível identificar padrões de performance e eficiência real da paralelização em cada abordagem, bem como os limites de escalabilidade para diferentes volumes de dados.

## **Resultados e Discussão**
Os testes conduzidos com três tamanhos de amostras de texto (10%, 50% e 100%) trouxeram resultados obtidos a partir da execução dos algoritmos que confirmaram o comportamento esperado em relação ao paralelismo e escalabilidade. O algoritmo Serial CPU apresentou desempenho consistente com complexidade linear O(n), mantendo tempos de execução proporcionais ao tamanho dos dados em todas as amostras. Já as versões paralelas demonstraram comportamentos distintos conforme o volume de dados processados.
Para amostras pequenas (10% do texto), o algoritmo Serial CPU apresentou melhor desempenho, seguido pela Parallel GPU. Este comportamento pode ser atribuído ao overhead de inicialização e comunicação entre threads nas abordagens paralelas, que se torna mais significativo em relação ao tempo total de processamento quando o volume de dados é reduzido. A Parallel CPU mostrou tempos intermediários, indicando que o custo de gerenciamento de threads não é compensado pelo ganho em paralelismo em cargas leves.
<table>
  <tr>
    <td><img scr="https://github.com/Fillipe143/ContadorDePalavras/blob/main/res/graficos/comparacao_amostra_10.png" width= "100%"></td>
    </tr>
</table>
Para amostras médias (50% do texto), observa-se uma inversão na hierarquia de desempenho. A Parallel GPU assume a liderança, demonstrando sua eficiência em processamento massivo de dados. A Parallel CPU apresenta melhoria significativa em relação à versão serial, reduzindo o tempo de processamento em aproximadamente 40-50%. O algoritmo Serial CPU mantém comportamento linear, porém com tempos superiores às abordagens paralelas.
Para amostras completas (100% do texto), a superioridade da Parallel GPU torna-se ainda mais evidente, processando os textos completos em tempos significativamente inferiores. A Parallel CPU consolida sua vantagem sobre a versão serial, com ganhos de desempenho que variam entre 55-65% dependendo do arquivo processado.
<table>
  <tr>
    <td><img src="https://github.com/Fillipe143/ContadorDePalavras/blob/main/res/graficos/comparacao_amostra_50.png" width="100%"></td>
    <td><img src="https://github.com/Fillipe143/ContadorDePalavras/blob/main/res/graficos/comparacao_amostra_100.png" width="100%"></td>
  </tr>
</table>
Analise de escalabilidade como comportamento com aumento de Threads fez com que a paralelização produzisse efeitos distintos conforme o tamanho das amostras analisadas. Para amostras pequenas, a curva de escalabilidade demonstra ganhos limitados com o aumento do número de threads. A partir de 4 threads, os benefícios adicionais tornam-se marginalmente decrescentes, indicando que o overhead de gerenciamento começa a superar os ganhos de paralelismo para cargas reduzidas.
 <table>
  <tr>
    <td><img scr="https://github.com/Fillipe143/ContadorDePalavras/blob/main/res/graficos/escalabilidade_amostra_10.png" width= "100%">       </td>
 </tr>
</table>
Para amostras médias, observa-se uma escalabilidade mais eficiente, com reduções consistentes no tempo de processamento até 8 threads. A lei de Amdahl é claramente visível, onde a parte paralelizável do algoritmo é melhor aproveitada com volumes intermediários de dados.
Para amostras completas, a escalabilidade atinge seu máximo potencial, demonstrando reduções quase lineares no tempo de processamento até 16 threads. Isto indica que para grandes volumes de dados, o algoritmo possui alta eficiência de paralelização, com ganhos significativos mesmo com número elevado de threads.
<table>
  <tr>
    <td><img src="https://github.com/Fillipe143/ContadorDePalavras/blob/main/res/graficos/escalabilidade_amostra_50.png" width="100%"></td>
    <td><img src="https://github.com/Fillipe143/ContadorDePalavras/blob/main/res/graficos/escalabilidade_amostra_100.png" width="100%"></td>
  </tr>
</table>
A Parallel GPU demonstrou superioridade indiscutível em todas as amostras de tamanho médio e grande, beneficiando-se do processamento massivamente paralelo. Contudo, nas amostras pequenas, o overhead de transferência de dados entre CPU e GPU impactou negativamente o desempenho, resultando em tempos superiores ao algoritmo Serial CPU.
A análise de escalabilidade revelou que o benefício da paralelização aumenta com o tamanho do texto processado. O ponto de equilíbrio onde a paralelização se torna vantajosa varia entre CPU e GPU - para a CPU paralela, este ponto ocorre em amostras médias (50%), enquanto para a GPU o benefício torna-se significativo apenas em amostras maiores.
A eficácia da paralelização foi significativamente influenciada pela natureza da tarefa de busca de padrões em texto. A operação de comparação de caracteres, sendo uma operação simples e independente, beneficiou-se do paralelismo de dados implementado tanto na CPU quanto na GPU. Contudo, o overhead de comunicação entre threads mostrou-se um fator crítico, particularmente evidente nas amostras menores.
A implementação em GPU apresentou vantagem clara para processamento de grandes volumes, mas a complexidade de programação em OpenCL/JOCL e o custo de transferência de dados limitam sua aplicabilidade em cenários com textos pequenos ou médios, onde a simplicidade da implementação serial ou paralela em CPU pode ser preferível.

## **Conclusão**
Com a análise comparativa abrangente do desempenho de algoritmos de busca de palavras em textos, comparando abordagens serial, paralela em CPU e paralela em GPU. Os resultados obtidos permitem extrair conclusões significativas sobre a eficácia das diferentes estratégias de processamento.
A implementação serial demonstrou ser a abordagem mais eficiente para volumes reduzidos de dados (10% do texto), onde o overhead associado ao paralelismo supera os benefícios da concorrência. Sua simplicidade de implementação e baixo custo computacional a tornam adequada para aplicações que processam pequenos volumes de texto ou operam em ambientes com recursos limitados.
As versões paralelas em CPU revelaram ganhos substanciais de desempenho conforme aumenta o volume de dados processados. A escalabilidade observada seguiu os princípios da Lei de Amdahl, com melhorias significativas até 8 threads para amostras médias e grandes. No entanto, os resultados também evidenciaram limitações práticas, como a diminuição dos ganhos marginais com o aumento excessivo do número de threads, particularmente visível em textos menores.
A implementação em GPU destacou-se no processamento de grandes volumes de dados (100% do texto), onde sua arquitetura massivamente paralela mostrou superioridade incontestável. Contudo, o overhead de transferência de dados entre CPU e GPU tornou esta abordagem menos eficiente para textos pequenos, indicando que o benefício do paralelismo em GPU é diretamente proporcional ao volume de dados processados.
Para aplicações com textos pequenos: A abordagem serial permanece a mais adequada.
Para processamento de textos médios: A paralelização em CPU com 4-8 threads oferece o melhor equilíbrio.
Para grandes volumes de texto: A GPU apresenta vantagem clara em desempenho.
Os resultados também destacam a importância de considerar as características específicas da aplicação - incluindo tamanho médio dos textos, frequência de execução e recursos hardware disponíveis - na seleção da estratégia de implementação mais adequada.
Em trabalhos futuros, seria interessante explorar estratégias híbridas que combinem as vantagens das diferentes abordagens, bem como investigar técnicas de pré-processamento e otimização que possam reduzir o overhead associado ao paralelismo, particularmente na transferência de dados para a GPU.

## **Referências** 
[1] ZHANG, A. et al. **Múltiplas GPUs**. In: #Dive into Deep Learning#, 2023.
[Disponível em:](https://pt.d2l.ai/chapter_computational-performance/multiple-gpus.html)

[2] AI FUTURE SCHOOL. **Processamento Paralelo em Linguagens Modernas**. 2023.
[Disponível em:](https://www.ai-futureschool.com/pt/informatica/processamento-paralelo-em-linguagens-modernas.php)

[3] RAZOR. **Guia de Otimização de Renderização de GPU.** Razor Blog, 2023.
[Disponível em:](https://blog.razor.com.br/edicao-de-video-motion-e-3d/guia-de-otimizacao-de-renderizacao-de-gpu/)

[4] FIBERMALL. **Clusters de GPU: O que São e Como Funcionam.** 2023.
[Disponível em:](https://www.fibermall.com/pt/blog/gpu-clusters.htm)

[5] ZHANG, A. et al. **Paralelismo Automático.** In: #Dive into Deep Learning#, 2023.
[Disponível em:](https://pt.d2l.ai/chapter_computational-performance/auto-parallelism.html)

[6] AKITA, F. **Concorrência e Paralelismo - Parte 1**. AkitaOnRails, 2019.
[Disponível em:](https://akitaonrails.com/2019/03/13/akitando-43-concorrencia-e-paralelismo-parte-1-entendendo-back-end-para-iniciantes-parte-3/)

[7] SILVA, J. **Dividindo LLMs em Múltiplas GPUs: Técnicas, Ferramentas e Melhores Práticas**. LinkedIn, 2023.
[Disponível em:](https://www.linkedin.com/pulse/splitting-llms-across-multiple-gpus-techniques-tools-best-yjikc/)

## **Anexos**

[https://github.com/Fillipe143/ContadorDePalavras/](https://github.com/Fillipe143/ContadorDePalavras/)
