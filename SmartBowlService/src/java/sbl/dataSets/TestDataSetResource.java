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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

// A special data set that produces dummy test data
public class TestDataSetResource extends DataSetResource {
    
    public static final String TEST_DATA_SET_ID = "test";
    
    public TestDataSetResource( DataSetsDAO dataSetsDAO ) {
        super(dataSetsDAO, TEST_DATA_SET_ID);
    }
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getDataSet( 
            @DefaultValue("0") @QueryParam("numEntries") int numEntries,
            @DefaultValue("0") @QueryParam("hoursBack") int hoursBack
        ) {
        getDataSetsDAO().clearDataSet(TEST_DATA_SET_ID);
        new TestDataGenerator( getDataSetsDAO() ).fillDataSet( getId(), hoursBack );
        return super.getDataSet(numEntries, hoursBack);
    }
    
}
