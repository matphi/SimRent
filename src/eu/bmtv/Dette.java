package eu.bmtv;

/**
 * Created with IntelliJ IDEA.
 * User: Propriétaire
 * Date: 23/11/13
 * Time: 18:24
 * To change this template use File | Settings | File Templates.
 */
public class Dette {
    int dateDebut;
    boolean locative;
    Emprunt emprunt;

    Dette(Emprunt emprunt, int dateDebut, boolean locative) {
        this.emprunt = emprunt;
        this.dateDebut = dateDebut;
        this.locative = locative;
    }
}
