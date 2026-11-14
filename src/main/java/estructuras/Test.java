package estructuras;

import estructuras.Cola.Cola;
import estructuras.Cola.ColaPrioritaria;
import estructuras.Grafo.Grafo;
import estructuras.Grafo.NodoGrafo;
import estructuras.Grafo.Tipo_nodos.Cuartos_aseo;
import estructuras.Grafo.Tipo_nodos.Pasillo;
import estructuras.Grafo.Tipo_nodos.Recepcion;
import modelo.Apartamento;

public class Test {
    public static void main(String[] args) {
        test_colaprio();
    }
    public static void test_grafo(){
        Apartamento apartamento=new Apartamento("123456");
        Cuartos_aseo cuartos_aseo=new Cuartos_aseo(1);
        Pasillo pasillo=new Pasillo(1);
        Pasillo pasillo2=new Pasillo(2);
        Pasillo pasillo3=new Pasillo(3);
        Apartamento apartamento4=new Apartamento("112233");
        Cuartos_aseo cuartos_aseo4=new Cuartos_aseo(4);
        Recepcion recepcion=new Recepcion(1);
        Apartamento apartamento5=new Apartamento("445566");
        Cuartos_aseo cuartos_aseo5=new Cuartos_aseo(5);

        NodoGrafo<Apartamento> nodo=new NodoGrafo<>(apartamento);
        NodoGrafo<Cuartos_aseo> nodo2=new NodoGrafo<>(cuartos_aseo);
        NodoGrafo<Pasillo> nodo3=new NodoGrafo<>(pasillo);
        NodoGrafo<Recepcion> nodo4=new NodoGrafo<>(recepcion);
        NodoGrafo<Apartamento> nodo5=new NodoGrafo<>(apartamento5);
        NodoGrafo<Cuartos_aseo> nodo6=new NodoGrafo<>(cuartos_aseo5);
        NodoGrafo<Pasillo> nodo7=new NodoGrafo<>(pasillo3);
        NodoGrafo<Recepcion> nodo8=new NodoGrafo<>(recepcion);
        NodoGrafo<Apartamento> nodo9=new NodoGrafo<>(apartamento4);
        NodoGrafo<Cuartos_aseo> nodo10=new NodoGrafo<>(cuartos_aseo4);
        NodoGrafo<Pasillo> nodo11=new NodoGrafo<>(pasillo2);
        NodoGrafo<Recepcion> nodo12=new NodoGrafo<>(recepcion);
        Grafo grafo=new Grafo();
        grafo.agregarNodo(nodo,nodo2,1);
        grafo.agregarNodo(nodo2,nodo3,2);
        grafo.agregarNodo(nodo3,nodo4,3);
        grafo.agregarNodo(nodo4,nodo,4);
        grafo.agregarNodo(nodo4,nodo2,6);
        grafo.agregarNodo(nodo5,nodo6,1);
        grafo.agregarNodo(nodo6,nodo7,2);
        grafo.agregarNodo(nodo7,nodo8,3);
        grafo.agregarNodo(nodo8,nodo4,4);
        grafo.agregarNodo(nodo8,nodo6,6);
        grafo.agregarNodo(nodo9,nodo10,1);
        grafo.agregarNodo(nodo10,nodo11,2);
        grafo.agregarNodo(nodo11,nodo12,3);
        grafo.agregarNodo(nodo12,nodo9,4);
        grafo.agregarNodo(nodo4,nodo10,6);

        ListaDoblementeEnlazada<NodoGrafo<?>> GrafoList = grafo.obtenerGrafo(nodo,new ListaDoblementeEnlazada<NodoGrafo<?>>());
        for (NodoGrafo<?> nodoGrafo : GrafoList) {
            System.out.println(nodoGrafo);
        }
        System.out.println(grafo.imprimir(nodo4));
    }
    public static void test_cola() {
        Cola<Integer> cola = new Cola<>();
        cola.add(1);
        cola.add(2);
        cola.add(3);
        cola.add(4);
        cola.add(5);
        cola.add(6);
        cola.add(7);
        cola.add(8);
        cola.add(9);
        cola.add(10);
        System.out.println(cola);
        cola.poll();
        cola.poll();
        cola.poll();
        System.out.println(cola);
        cola.add(1);
        cola.add(2);
        cola.add(3);
        System.out.println(cola);
        Cola<Integer> cola2 = new Cola<>();
        cola2.add(11);
        cola2.add(12);
        cola2.add(13);
        cola2.add(14);
        cola2.add(15);
        cola.merge(cola2);
        System.out.println(cola);
    }
    public static void test_colaprio(){
        ColaPrioritaria<Integer> cola=new ColaPrioritaria<>();
        cola.add(1);
        cola.add(2);
        cola.add(3);
        cola.add(4);
        cola.add(5);
        cola.add(6);
        cola.add(7);
        cola.add(8);
        cola.add(9);
        cola.add(10);
        System.out.println(cola);
        cola.poll();
        cola.poll();
        cola.poll();
        cola.poll();
        System.out.println(cola);
        cola.add(11);
        cola.add(12);
        cola.add(13);
        cola.add(1);
        cola.add(2);
        cola.add(3);
        System.out.println(cola);
        cola.add(4);
        System.out.println(cola);
    }
}
