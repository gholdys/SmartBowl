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

package sbl.dashboards;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

@Path("dashboards/{dataSet}")
public class DashboardPageResource {

    @Context
    ServletContext context;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public StreamingOutput getDashboardPage(@PathParam("dataSet") String id) throws Exception {
        return new StreamingOutput() {
            public void write(OutputStream output) throws IOException, WebApplicationException {                
                Map <String,String> dashboardProperties = new HashMap<>();
                dashboardProperties.put("id", id);
                
                Writer writer = new OutputStreamWriter( output );
                MustacheFactory mf = new DefaultMustacheFactory();
                InputStream templateStream = context.getResourceAsStream("/WEB-INF/dashboard.html");
                Mustache mustache = mf.compile( new InputStreamReader( templateStream ), "dashboard" );
                mustache.execute(writer, dashboardProperties);
                writer.flush();
            }
        };
    }
    
}
