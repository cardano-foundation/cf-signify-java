package e2e;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class KeriaApiResponseTest {

    @Test
    public void testKeriaApiResponseStructure() {
        String keriaUrl = "http://localhost:8080/api/keria";

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(keriaUrl).openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonResponse = new JSONObject(response.toString());

            // Expected fields that may not yet be implemented in KERIA
            Assert.assertTrue(jsonResponse.has("status"), "Missing 'status' field in KERIA response.");
            Assert.assertTrue(jsonResponse.has("data"), "Missing 'data' field in KERIA response.");

        } catch (Exception e) {
            Assert.fail("Error processing KERIA API response.", e);
        }
    }
}
