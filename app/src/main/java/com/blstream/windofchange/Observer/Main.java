package com.blstream.windofchange.Observer;

/**
 *
 */
public class Main {
    public static void main(String[]args){

        TotoLotek totoLotek = new TotoLotek();
        Telewizja telewizja = new Telewizja(totoLotek);
        Internet internet = new Internet(totoLotek);

        totoLotek.dodajObserwatora(telewizja);
        totoLotek.dodajObserwatora(internet);

        totoLotek.kolejneLosowanie();
        System.out.println("LOSOWANIE 1\n");
        telewizja.informuj();
        internet.informuj();

        totoLotek.kolejneLosowanie();
        System.out.println("\nLOSOWANIE 2\n");
        telewizja.informuj();
        internet.informuj();

/* telewizja juz nie obserwuuje wynikow, nie bedzie miec aktualnych losowan */
        telewizja.spadam();
        totoLotek.kolejneLosowanie();
        System.out.println("\nLOSOWANIE 3\n");
        telewizja.informuj();
        internet.informuj();
    }
}