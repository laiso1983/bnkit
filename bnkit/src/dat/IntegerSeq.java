/*
    bnkit -- software for building and using Bayesian networks
    Copyright (C) 2014  M. Boden et al.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dat;

import bn.prob.EnumDistrib;

/**
 * Domain definition for a real vector--a sequence of continuous elements. 
 * For checking validity of values for variables that belong to this domain.
 */
public class IntegerSeq extends SeqDomain<Continuous> {

    public IntegerSeq(Continuous elementType) {
        super(elementType);
    }

    public IntegerSeq(int[] hist) {
        super(new Continuous());
        Object[] arr = new Object[hist.length];
        for (int i = 0; i < arr.length; i ++)
            arr[i] = hist[i];
        set(arr);
    }
    
    public boolean isValid(Object value) {
        try {
            Iterable iter = (Iterable)value;
            for (Object elem : iter) {
                Integer x = (Integer) elem;
            }
            return true;
        } catch (ClassCastException e1) {
            try {
                Object[] iter = (Object[])value;
                for (Object elem : iter) {
                    Integer x = (Integer) elem;
                }            
                return true;
            } catch (ClassCastException e2) {
                return false;
            }
        }
    }
    
    public static int[] intArray(Object[] arr) {
        int[] values = new int[arr.length];
        for (int i = 0; i < arr.length; i ++)
            values[i] = (Integer)arr[i];
        return values;
    }

    public static Object[] objArray(int[] arr) {
        Object[] values = new Object[arr.length];
        for (int i = 0; i < arr.length; i ++)
            values[i] = (Integer)arr[i];
        return values;
    }
    
    public static IntegerSeq intSeq(int[] hist) {
        IntegerSeq is = new IntegerSeq(new Continuous());
        Object[] arr = new Object[hist.length];
        for (int i = 0; i < arr.length; i ++)
            arr[i] = hist[i];
        is.set(arr);
        return is;
    }

    //To handle loading data for DirDT nodes where an IntegerSeq and not a
    //EnumDistrib is required
    public static IntegerSeq parseIntegerSeq(String str, EnumDistrib dom) {
        String[] parts = str.split("\\|");
        if (parts.length != dom.getDomain().size())
            throw new RuntimeException("Invalid specification of distribution: " + str);
        int[] values = new int[parts.length];

        for (int i = 0; i < values.length; i ++) {
            try {
                values[i] = Integer.valueOf(parts[i]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid parameter for distribution: " + parts[i] + " in " + str);
            }
        }
        return new IntegerSeq(values);
    }
}
