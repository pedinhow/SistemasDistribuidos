package q3;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RingLauncher {

    public static void main(String[] args) {
        int numNodes = 6;
        int basePort = 6000;
        List<P2PNode> nodes = new ArrayList<>();

        System.out.println("Iniciando " + numNodes + " nós P2P na topologia em anel...");

        // 1. Criar os Nós
        for (int i = 0; i < numNodes; i++) {
            int myPort = basePort + i;
            // O sucessor do último nó (P5) é o primeiro (P0)
            int successorPort = basePort + ((i + 1) % numNodes);

            P2PNode node = new P2PNode(i, myPort, successorPort);
            nodes.add(node);
        }

        // 2. Iniciar os Nós em threads separadas
        for (P2PNode node : nodes) {
            new Thread(node).start();
        }

        System.out.println("\nAnel P2P configurado e rodando!");
        System.out.println("P0 (Porta 6000) -> P1 (Porta 6001) -> ... -> P5 (Porta 6005) -> P0");

        // 3. Console de Interação
        System.out.println("\nPara iniciar uma busca, use o formato: ID_DO_NO:NOME_DO_ARQUIVO");
        System.out.println("Exemplo: 0:arquivo25 (Nó P0 busca pelo arquivo 25)");
        System.out.println("Exemplo: 4:arquivo12");
        System.out.println("Digite 'SAIR' para terminar.");

        Scanner teclado = new Scanner(System.in);
        while (true) {
            try {
                String input = teclado.nextLine();
                if (input.equalsIgnoreCase("SAIR")) {
                    break;
                }

                String[] parts = input.split(":");
                if (parts.length != 2) {
                    //
                    System.out.println("Erro na entrada de dados. Tente outra vez!");
                    continue;
                }

                int nodeId = Integer.parseInt(parts[0]);
                String fileName = parts[1].trim();

                if (nodeId < 0 || nodeId >= numNodes) {
                    //
                    System.out.println("Erro na entrada de dados. ID do Nó deve ser entre 0 e 5.");
                    continue;
                }

                // Encontra o nó e inicia a busca
                P2PNode startingNode = nodes.get(nodeId);
                // Inicia a busca em uma nova thread para não bloquear o console
                new Thread(() -> startingNode.startSearch(fileName)).start();

            } catch (NumberFormatException e) {
                System.out.println("Erro na entrada de dados. Formato inválido."); //
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }

        System.out.println("Encerrando launcher... (Nós continuarão rodando em background)");
        teclado.close();
        System.exit(0);
    }
}
