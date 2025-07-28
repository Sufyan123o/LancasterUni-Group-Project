package g82.models;

// Interface that handles saving states for undo/redo
public interface IStateModel {
    public static enum Locality {
        GLOBAL,
        LOCAL,
        LAYER
    }

    public Locality GetLocality();

}
