# "Cabra Cega VR"

(Nome provisório pq é feio)
Projeto de FSI

Integrantes:

 - André
 - Lucas
 - Sérgio Kazuo Nomura     USP: 10816781
 - Vitor

## Histórico do Projeto
(Escreva uma breve descrição das versões e mudanças ocorridas durante o projeto desde o início.)

## Interface
### Sistema Visual
HUD(Head-Up Display)
 - Tempo 
 - Menus
   - Opções
   - Instruções
   - Créditos
   - Parear Celular
   - Iniciar Jogo
Câmera
 - Visão em primeira pessoa usando a câmera do celular.
### Sistema de Controle
Andar tendo noção do espaço a ser utilizado.

## Projeto Técnico
### Equipamento-alvo
Smartphone Android (IOS possivelmente em versões futuras), Versões KitKat(4.4)/Lollipop(5.0) pra cima.
Smartphone com Bluetooth, acesso câmera que filme em ao menos 480p, acesso a Wi-fi, Tela de ao menos 5 polegadas, giroscópio.
### Ambiente desenvolvido (Hardware e Software)
PC Windows 10, 8-16GB ram, Intel Core i5-i7 ou Arch Linux, 2-4GB ram, Intel Core i3-i7.
### Procedimentos e padrões de Desenvolvimento
O jogo devera ser desenvolvido respeitando seu conceito inicial. Deverá possuir um menu principal contendo botões para que possa seguir o fluxo do software.
 - A opção de "Instruções" terá apenas textos que poderão ser fixos ou passados para o entendimento do jogo ao jogador.
 - A opção de "Créditos" terá apenas textos fixos contendo o nome e o número USP de cada integrante envolvido no projeto, incluindo nome do professor e a matéria FSI.
 - A opção "Parear celular" terá texto e botão para que o celular se conecte ao "Wi-fi" ou ao "Bluetooth", estabelecendo conexão com outro celular também nessas redes. Podendo também desparear o celular.
 - A opção "Opções" mostrará a qualidade da conexão do celular com outro, podendo reduzir ou aumentar a qualidade, além de ter um feedback se o celular está conectado ou não.
 - A opção "Iniciar Jogo" irá verificar se o celular está conectado com outro, e em caso negativo, mandará o jogador de volta ao menu com um aviso que é necessária a conexão. Em caso positivo, será ativada a câmera para a tela do celular, será mandada a imagem captada para o celular conectado, e essa imagem, ao chegar no celular, receberá um "tratamento" para que se torne VR, sendo dividida em duas imagens cortadas para cada olho. (Necessita de pesquisa). Confirmando os procedimentos da imagem, ambos estarão "cegos" com fundo preto em seus olhos, com uma pequena contagem regressiva de 5 segundos avisando que a imagem irá ser liberada. No final da contagem o jogo se inicia, com um timer de 1 minuto(teste) para que a brincadeira se complete. No final o jogo retorna ao menu principal.
### Motor do Jogo (Engine)
Descreva qual a engine utilizada para criar o jogo e sua versão.
### Rede
Descreva o ambiente de rede em que o jogo está, expondo servidores (no caso de Multiplayers e MMOs), se será via internet, apenas intranet ou VPN, entre outros.
### Linguagem de programação
O código-fonte comentado é inserido na Script Bible produzido pela equipe de programadores.

## Softwares Secundário
### Editores (Ex.: Modelagem 2D ou 3D, sons, músicas)
Android Studio, vis.
### Instaladores
Google Play / apk manual.
### Atualização de programas
Google Play / apk manual.
### Miscelânea

