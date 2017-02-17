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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;

public class DataSetsDAO {
    
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");    
    private static final DataSetsDAO INSTANCE = new DataSetsDAO();
    private static final int MAX_ENTRY_COUNT = 30000;
    private static final String CSV_HEADER = "time,amount,consumed,added,refills";
    
    public static DataSetsDAO getInstance() {
        return INSTANCE;
    }
    
    private final Map <String, Queue<DataSetEntry>> dataSetLookup = new HashMap<>();
    
    private DataSetsDAO() {};
    
    public synchronized List <DataSetEntry> getDataSet( String deviceId ) {
        Queue <DataSetEntry> dataSet = dataSetLookup.get(deviceId);
        if ( dataSet != null ) {
            return new ArrayList<>( dataSet );
        } else {
            return new ArrayList<>();
        }
    }
    
    public synchronized void clearDataSet( String deviceId ) {
        Queue <DataSetEntry> dataSet = dataSetLookup.get(deviceId);
        if ( dataSet != null ) {
            dataSet.clear();
        }
    }
    
    public synchronized void addToDataSet( String deviceId, double amountRemaining, double amountConsumed, double amountAdded, int numRefills ) {
        ZonedDateTime utcNow = ZonedDateTime.now( UTC_ZONE );
        long timestamp = utcNow.toEpochSecond();
        addToDataSet( deviceId, timestamp, amountRemaining, amountConsumed, amountAdded, numRefills);
    }
    
    public List<DataSetEntry> getRawEntries( String deviceId, ZonedDateTime from ) {
        return getRawEntries( deviceId, from, ZonedDateTime.now(from.getZone()) );
    }
    
    public List<DataSetEntry> getRawEntries( String deviceId, ZonedDateTime from, ZonedDateTime to ) {
        Queue<DataSetEntry> dataSet = dataSetLookup.get( deviceId );
        if ( dataSet == null ) return new ArrayList<>();
        List <DataSetEntry> ret = dataSet.stream().filter( dse -> {
            ZonedDateTime timestamp = ZonedDateTime.ofInstant( Instant.ofEpochSecond( dse.getTimestamp() ), UTC_ZONE );
            return timestamp.isAfter( from ) && timestamp.isBefore( to );            
        } ).collect(
            Collectors.toList()
        );
        return ret;
    }
    
    public List<DataSetEntry> getRawEntries( String deviceId, int numEntries ) {
        Queue<DataSetEntry> dataSet = dataSetLookup.get( deviceId );
        if ( dataSet == null ) return new ArrayList<>();
        
        List<DataSetEntry> entries = new ArrayList<>(dataSet);
        
        int offset;
        // Passing "0" or a negative value causes the service to return the whole series
        if ( numEntries <= 0 ) numEntries = entries.size();
        
        if ( numEntries < entries.size() ) {
            offset = entries.size() - numEntries;
        } else {
            offset = 0;
            numEntries = entries.size();
        }
        
        List <DataSetEntry> ret = new ArrayList<>();
        for ( int i=0; i<numEntries; i++ ) {
            ret.add( entries.get( offset+i ) );
        }
        return ret;
    }
    
    public String getRawEntriesCSV( String deviceId, int numEntries ) {
        Queue<DataSetEntry> dataSet = dataSetLookup.get( deviceId );
        StringBuilder b = new StringBuilder();
        b.append( CSV_HEADER ).append( "\n" );
        if ( dataSet != null ) {            
            dataSet.forEach( e -> appendCsvRow(b, e) );
        }
        return b.toString();
    }
    
    public List<DataSetEntry> getPerHourEntries( String deviceId, int hoursBack ) {
        if ( hoursBack <= 0 ) return new ArrayList<>();
        
        List<DataSetEntry> entries = new ArrayList<>(dataSetLookup.get( deviceId ));
        ZonedDateTime utcNow = ZonedDateTime.now();
        ZonedDateTime utcStartTime = utcNow.minusHours(hoursBack);
        
        List <DataSetEntry> ret = entries
            .stream()
            .filter( dse -> ZonedDateTime.ofInstant( Instant.ofEpochSecond( dse.getTimestamp() ), UTC_ZONE ).isAfter( utcStartTime ) )
            .collect( Collectors.groupingBy( dse -> ZonedDateTime.ofInstant( Instant.ofEpochSecond( dse.getTimestamp() ), UTC_ZONE ).getHour() ))
            .entrySet()
            .stream()
            .map(e -> e.getValue().stream()
                .reduce( (dse1,dse2) -> new DataSetEntry(
                    Math.max( dse1.getTimestamp(), dse2.getTimestamp() ),
                    dse1.getTimestamp() > dse2.getTimestamp() ? dse1.getAmountRemaining() : dse2.getAmountRemaining(),
                    dse1.getAmountConsumed()+dse2.getAmountConsumed(),
                    dse1.getAmountAdded()+dse2.getAmountAdded(),
                    dse1.getNumRefills()+dse2.getNumRefills()
            ) ) )
            .map(f -> f.get())
            .sorted( (dse1,dse2) -> Long.compare(dse1.getTimestamp(), dse2.getTimestamp()) )
            .collect(
                Collectors.toList()
            );
        
        return ret;
    }
    
    public String getPerHourEntriesCSV( String deviceId, int hoursBack ) {
        List <DataSetEntry> entries = getPerHourEntries(deviceId, hoursBack);
        StringBuilder b = new StringBuilder();
        b.append( CSV_HEADER ).append( "\n" );
        entries.forEach( dse -> appendCsvRow(b, dse) );        
        return b.toString();
    }
    
    protected synchronized void addToDataSet( String deviceId, long timestamp, double amountRemaining, double amountConsumed, double amountAdded, int numRefills ) {
        Queue <DataSetEntry> dataSet = dataSetLookup.get(deviceId);
        if ( dataSet == null ) {
            dataSet = new LinkedList<>();
            dataSetLookup.put(deviceId, dataSet);
        }
        dataSet.add( new DataSetEntry(timestamp, amountRemaining, amountConsumed, amountAdded, numRefills ) );
        if ( dataSet.size() > MAX_ENTRY_COUNT ) {
            dataSet.poll();
        }
    }
    
    private String formatDateTime( DataSetEntry e ) {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant( Instant.ofEpochSecond( e.getTimestamp() ), UTC_ZONE );
        return dateTime.format(DATETIME_FORMATTER);
    }
    
    private void appendCsvRow( StringBuilder b, DataSetEntry e ) {
        b.append( formatDateTime(e) ).append(",")
            .append( String.format( "%.2f", e.getAmountRemaining() ) ).append(",")
            .append( String.format( "%.2f", e.getAmountConsumed() ) ).append(",")
            .append( String.format( "%.2f", e.getAmountAdded() ) ).append(",")
            .append( String.format( "%d", e.getNumRefills() ) ).append("\n");
    }    
    
}
