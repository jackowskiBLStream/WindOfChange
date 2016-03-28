package com.blstream.windofchange.Observer;

/**
 *
 */
interface Obserwowany {
     void dodajObserwatora(Observer o);
     void usunObserwatora(Observer o);
     void powiadamiajObserwatorow();
}
