/*

MIT License

Copyright (c) 2017 Grzegorz Hołdys

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

*/

package sbl.dataSets;

public class DataSetEntry {
    
    private final long timestamp;
    private final double amountRemaining;
    private final double amountConsumed;
    private final double amountAdded;
    private final int numRefills;

    public DataSetEntry(long timestamp, double amountRemaining, double amountConsumed, double amountAdded, int numRefills) {
        this.timestamp = timestamp;
        this.amountRemaining = amountRemaining;
        this.amountConsumed = amountConsumed;
        this.amountAdded = amountAdded;
        this.numRefills = numRefills;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getAmountRemaining() {
        return amountRemaining;
    }

    public double getAmountConsumed() {
        return amountConsumed;
    }

    public double getAmountAdded() {
        return amountAdded;
    }

    public int getNumRefills() {
        return numRefills;
    }
    
}
