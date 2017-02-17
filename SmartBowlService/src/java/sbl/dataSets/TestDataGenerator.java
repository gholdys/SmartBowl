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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Random;

public final class TestDataGenerator {

    
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");    
    
    private final DataSetsDAO dataSetsDAO;
    private int numSampleEntries = 100;
    private double initialAmount = 50.0;

    
    public TestDataGenerator(DataSetsDAO dataSetsDAO) {
        this.dataSetsDAO = dataSetsDAO;
    }

    public void setInitialAmount(double initialAmount) {
        this.initialAmount = initialAmount;
    }

    public double getInitialAmount() {
        return initialAmount;
    }

    public void setNumSampleEntries(int numSampleEntries) {
        this.numSampleEntries = numSampleEntries;
    }

    public DataSetsDAO getDataSetsDAO() {
        return dataSetsDAO;
    }

    public int getNumSampleEntries() {
        return numSampleEntries;
    }
    
    public void fillDataSet( String dataSetId, int hoursBack ) {
        double amount = initialAmount;
        ZonedDateTime utcNow = ZonedDateTime.now( UTC_ZONE );
        ZonedDateTime startTime = utcNow.minusHours( hoursBack > 0 ? hoursBack : 24 );
        long startTimeSecond = startTime.toEpochSecond();
        int maxSecondsIncrement = (int) (2*(utcNow.toEpochSecond() - startTimeSecond)/numSampleEntries);
        Random r = new Random();
        long timestamp = startTimeSecond;
        for ( int i=0; i<numSampleEntries; i++ ) {
            
            double consumed = r.nextDouble() < 0.7 ? r.nextDouble()*10 : 0.0;
            consumed = Math.min(amount, consumed);
            
            double added = (amount < 20.0 && r.nextDouble() < 0.2) ? r.nextDouble()*20 : 0.0;
            amount += added-consumed;
            
            timestamp += r.nextDouble()*maxSecondsIncrement;
            dataSetsDAO.addToDataSet( dataSetId, timestamp, amount, consumed, added, 0 );
        }
    }
    
}
