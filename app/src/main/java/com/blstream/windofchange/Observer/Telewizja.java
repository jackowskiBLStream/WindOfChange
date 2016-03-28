package com.blstream.windofchange.Observer;

/**
 *
 */
/* obserwator */
class Telewizja implements Observer, Media {
    private int[] wyniki;
    private TotoLotek lotek;

    public Telewizja(TotoLotek lotek){
        wyniki = new int[6];
        this.lotek = lotek;
    }

    public void update(int[]tab){
        for(int i=0;i<6;i++){
            wyniki[i]=tab[i];
        }
    }

    public void informuj(){
        System.out.println("Dzisiejsze losowanie Totolotka by TVP");
        for(int i:wyniki)
            System.out.print(i + " ");
        System.out.println();
    }

    public void spadam(){
        lotek.usunObserwatora(this);
    }
}
