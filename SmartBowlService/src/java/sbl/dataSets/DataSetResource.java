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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class DataSetResource {

    
    private final DataSetsDAO dataSetsDAO;
    private final String deviceId;

    
    public DataSetResource( DataSetsDAO dataSetsDAO, String deviceId ) {
        this.dataSetsDAO = dataSetsDAO;
        this.deviceId = deviceId.toLowerCase();
    }

    public DataSetsDAO getDataSetsDAO() {
        return dataSetsDAO;
    }

    public String getId() {
        return deviceId;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getDataSet( 
            @DefaultValue("0") @QueryParam("numEntries") int numEntries,
            @DefaultValue("0") @QueryParam("hoursBack") int hoursBack
        ) {
        if ( ( numEntries == 0 && hoursBack == 0 ) || numEntries > 0 ) {
            return dataSetsDAO.getRawEntriesCSV(deviceId, numEntries);
        } else {
            return dataSetsDAO.getPerHourEntriesCSV(deviceId, hoursBack);
        }
    }
    
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteDataSet() {
        dataSetsDAO.clearDataSet(deviceId);
        return Response.ok().build();
    }
    
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Response addDataPoint( String dataLine ) {
        String[] items = dataLine.split(",");
        double amountRemaining = 0.0;
        double amountConsumed = 0.0;
        double amountAdded = 0.0;
        int numRefills = 0;
        if ( items.length > 0 ) amountRemaining = Double.parseDouble( items[0].trim() );
        if ( items.length > 1 ) amountConsumed = Double.parseDouble( items[1].trim() );
        if ( items.length > 2 ) amountAdded = Double.parseDouble( items[2].trim() );
        if ( items.length > 3 ) numRefills = Integer.parseInt( items[3].trim() );
        DataSetsDAO.getInstance().addToDataSet(deviceId, amountRemaining, amountConsumed, amountAdded, numRefills);
        return Response.ok().build();
    }
        
}
