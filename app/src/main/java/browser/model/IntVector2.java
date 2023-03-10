package browser.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class IntVector2 {

    public int x = 0;
    public int y = 0;

    public IntVector2 transpose() {
        return new IntVector2(y, x);
    }

}
