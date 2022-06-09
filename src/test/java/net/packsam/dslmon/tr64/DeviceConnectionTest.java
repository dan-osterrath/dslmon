package net.packsam.dslmon.tr64;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DeviceConnectionTest {
    @Test
    @Disabled("TODO: provide mock server")
    public void testReadDeviceSpec() throws IOException {
        var deviceConnection = new DeviceConnection("fritz.box", 49000);
        var device = deviceConnection.readDeviceSpec();
        assertNotNull(device);
        assertNotNull(device.getDeviceList());
    }
}
