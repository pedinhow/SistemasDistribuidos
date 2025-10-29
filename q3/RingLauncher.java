package q3;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RingLauncher {

    public static void main(String[] args) throws Exception {
        int numNodes = 6;
        int basePort = 6000;
        List<P2PNode> nodes = new ArrayList<>();

        System.out.println("Iniciando " + numNodes + " nós P2P na topologia em anel...");

        // criar os nós com sucessor e antecessor
        for (int i = 0; i < numNodes; i++) {
            int myPort = basePort + i;
            int successorPort = basePort + ((i + 1) % numNodes);
            int predecessorPort = basePort + ((i - 1 + numNodes) % numNodes);

            P2PNode node = new P2PNode(i, myPort, successorPort);
            nodes.add(node);
        }

        // iniciar os nós em threads separadas
        for (P2PNode node : nodes) {
            new Thread(node).start();
        }

        System.out.println("\nAnel P2P configurado e rodando!");
        System.out.println("P0 -> P1 -> P2 -> P3 -> P4 -> P5 -> P0");

        // console de interação para buscar arquivos
        Scanner teclado = new Scanner(System.in);
        System.out.println("\nPara buscar: ID_DO_NO:NOME_DO_ARQUIVO:HORARIO|ANTIHORARIO");
        System.out.println("Exemplo: 0:arquivo25:HORARIO (P0 busca arquivo25 sentido horário)");
        System.out.println("Digite 'SAIR' para encerrar.");

        while (true) {
            String input = teclado.nextLine();
            if (input.equalsIgnoreCase("SAIR")) break;

            String[] parts = input.split(":");
            if (parts.length != 3) {
                System.out.println("Erro na entrada de dados. Formato: ID_DO_NO:NOME_DO_ARQUIVO:HORARIO|ANTIHORARIO");
                continue;
            }

            try {
                int nodeId = Integer.parseInt(parts[0]);
                String fileName = parts[1].trim();
                String direction = parts[2].trim().toUpperCase();

                if (nodeId < 0 || nodeId >= numNodes) {
                    System.out.println("Erro: ID do nó deve ser entre 0 e 5.");
                    continue;
                }

                // inicia a busca no nó escolhido
                P2PNode startingNode = nodes.get(nodeId);
                new Thread(() -> startingNode.startSearch(fileName, direction)).start();

            } catch (NumberFormatException e) {
                System.out.println("Erro na entrada de dados. ID do nó deve ser um número.");
            }
        }

        System.out.println("Encerrando launcher... (nós continuam rodando em background)");
        teclado.close();
    }
}