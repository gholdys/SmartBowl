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

package sbl.configurations;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import sbl.dataSets.DataSetEntry;
import sbl.dataSets.DataSetsDAO;
import sbl.devices.Device;
import sbl.devices.DeviceDAO;

@Path("configurations")
public class ConfigurationsResource {


    public ConfigurationsResource() {
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{device}")
    public Response getConfiguraion( 
        @PathParam("device") String deviceId,
        @DefaultValue("rationLeft,secondsLeft") @QueryParam("fields") String fields
    ) {                
        Device device = DeviceDAO.getInstance().getDevice( deviceId );
        if ( device == null ) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        ZoneId deviceTZ = ZoneId.of( device.getTimeZone() );
        ZonedDateTime now = ZonedDateTime.now( deviceTZ );
        ZonedDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime endOfDay = startOfDay.plusHours(24);
        List<DataSetEntry> rawEntries = DataSetsDAO.getInstance().getRawEntries( deviceId, startOfDay );
        int consumedSoFar = calculateTotalConsumption(rawEntries);
        int dailyRation = device.getDailyRation();
        int rationLeft = Math.max( 0, dailyRation-consumedSoFar );
        int secondsLeft = (int) now.until(endOfDay, ChronoUnit.SECONDS);
        return Response.ok( formatResponse(fields, rationLeft, secondsLeft) ).build();
    }
    
    private int calculateTotalConsumption( List <DataSetEntry> entries ) {
        return (int) Math.round(entries.stream().map( DataSetEntry::getAmountConsumed ).reduce( 0.0, (a, b) -> a + b ));
    }
    
    private String formatResponse( String fields, int rationLeft, int secondsLeft  ) {
        String[] fieldArray = fields.split(",");
        StringBuilder b = new StringBuilder();
        for ( int i=0; i<fieldArray.length; i++ ) {
            String field = fieldArray[i];
            if ( field.equalsIgnoreCase("rationLeft") ) {
                b.append( rationLeft );
            } else if ( field.equalsIgnoreCase("secondsLeft") ) {
                b.append( secondsLeft );
            }
            b.append(",");
        }
        return b.substring(0, b.length()-1);  // -1 to remove the trailing comma
    }

}
