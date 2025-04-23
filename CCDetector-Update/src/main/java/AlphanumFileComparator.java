import java.io.File;
import java.util.Comparator;

public class AlphanumFileComparator<T> implements Comparator<T> {

    public String getUnit(String s, int strlength, int marker) {
        StringBuilder Unit = new StringBuilder();
        char c = s.charAt(marker);
        Unit.append(c);
        marker++;
        if (isDigit(c)) {
            while (marker < strlength) {
                c = s.charAt(marker);
                if (!isDigit(c))
                    break;
                Unit.append(c);
                marker++;
            }
        } else {
            while (marker < strlength) {
                c = s.charAt(marker);
                if (isDigit(c))
                    break;
                Unit.append(c);
                marker++;
            }
        }
        return Unit.toString();
    }

    @Override
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof File f1) || !(o2 instanceof File f2)) {
            return 0;
        }
        String s1 = f1.getName();
        String s2 = f2.getName();

        int thisMarker = 0;
        int thatMarker = 0;
        int s1Length = s1.length();
        int s2Length = s2.length();

        while (thisMarker < s1Length && thatMarker < s2Length) {
            String thisUnit = getUnit(s1, s1Length, thisMarker);
            thisMarker += thisUnit.length();

            String thatUnit = getUnit(s2, s2Length, thatMarker);
            thatMarker += thatUnit.length();


            int result;
            if (isDigit(thisUnit.charAt(0)) && isDigit(thatUnit.charAt(0))) {
                int thisUnitLength = thisUnit.length();
                result = thisUnitLength - thatUnit.length();
                if (result == 0) {
                    for (int i = 0; i < thisUnitLength; i++) {
                        result = thisUnit.charAt(i) - thatUnit.charAt(i);
                        if (result != 0) {
                            return result;
                        }
                    }
                }
            } else {
                result = thisUnit.compareTo(thatUnit);
            }

            if (result != 0)
                return result;
        }

        return s1Length - s2Length;
    }

    public boolean isDigit(char ch) {
        return ch >= 48 && ch <= 57;
    }


}