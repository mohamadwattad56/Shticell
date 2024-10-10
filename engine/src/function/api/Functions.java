package function.api;

import cell.api.Cell;

import java.io.Serializable;

public interface Functions extends Serializable {
     Object apply(Cell... args);
     String getName();
}
