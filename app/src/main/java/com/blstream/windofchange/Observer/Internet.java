package com.blstream.windofchange.Observer;

/**
 *
 */
class Internet implements Observer, Media {
    private int[] wyniki;
    private TotoLotek lotek;

    public Internet(TotoLotek lotek){
        wyniki = new int[6];
        this.lotek = lotek;
    }

    public void update(int[] tab){
        for(int i=0;i<6;i++){
            wyniki[i]=tab[i];
        }
    }

    public void informuj(){
        System.out.println("Wyniki TotoLotka na stronie internetowej");
        for(int i:wyniki)
            System.out.print(i + " ");
        System.out.println();
    }

    public void spadam(){
        lotek.usunObserwatora(this);
    }
}

