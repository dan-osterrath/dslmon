package net.packsam.dslmon.commands;

import net.packsam.dslmon.DSLMonitor;
import net.packsam.dslmon.device.IGDClient;
import net.packsam.dslmon.device.TR64Client;
import net.packsam.dslmon.device.jaxb.tr64.device.Device;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(
        name = "inspect",
        description = "Inspects the DSL device"
)
public class InspectCommand implements Callable<Integer> {

    @SuppressWarnings("unused")
    @ParentCommand
    private DSLMonitor parent;

    @Override
    public Integer call() throws Exception {
        var host = parent.getHost();
        var port = parent.getPort();

        var tr64Client = new TR64Client(host, port);

        var tr64DeviceSpec = tr64Client.readDeviceSpec();
        var tr64Device = tr64DeviceSpec.getDevice();
        System.out.println("Found TR-064 device:");
        printTR64DeviceInfo(tr64Device, tr64Client, "  ");

        var igdClient = new IGDClient(host, port);

        var igdDeviceSpec = igdClient.readDeviceSpec();
        var igdDevice = igdDeviceSpec.getDevice();
        System.out.println("Found IGD device:");
        printIGDDeviceInfo(igdDevice, igdClient, "  ");

        return 0;
    }

    private void printTR64DeviceInfo(Device device, TR64Client client, String prefix) {
        var nextPrefix = prefix + "  ";

        System.out.printf("%s\uD83D\uDCE0 %s %s\n", prefix, device.getManufacturer(), device.getFriendlyName());

        var serviceList = device.getServiceList();
        if (serviceList != null) {
            serviceList.getService().forEach(service -> {
                System.out.printf("%s\uD83D\uDC81 %s\n", nextPrefix, service.getServiceType());

                try {
                    var scpd = client.readServiceSpec(service);
                    
                    var actionList = scpd.getActionList();
                    if (actionList != null) {
                        actionList.getAction().forEach(action -> System.out.printf("%s  \uD83D\uDD28 %s\n", nextPrefix, action.getName()));
                    }
                    
                    var serviceStateTable = scpd.getServiceStateTable();
                    if (serviceStateTable != null) {
                        serviceStateTable.getStateVariable().forEach(stateVariable -> System.out.printf("%s  \uD83D\uDCC8 %s\n", nextPrefix, stateVariable.getName()));
                    }
                } catch (IOException ignored) {
                }
            });
        }

        var deviceList = device.getDeviceList();
        if (deviceList != null) {
            deviceList.getDevice().forEach(subDevice -> printTR64DeviceInfo(subDevice, client, nextPrefix));
        }
    }

    private void printIGDDeviceInfo(net.packsam.dslmon.device.jaxb.igd.device.Device device, IGDClient client, String prefix) {
        var nextPrefix = prefix + "  ";

        System.out.printf("%s\uD83D\uDCE0 %s %s\n", prefix, device.getManufacturer(), device.getFriendlyName());

        var serviceList = device.getServiceList();
        if (serviceList != null) {
            serviceList.getService().forEach(service -> {
                System.out.printf("%s\uD83D\uDC81 %s\n", nextPrefix, service.getServiceType());

                try {
                    var scpd = client.readServiceSpec(service);

                    var actionList = scpd.getActionList();
                    if (actionList != null) {
                        actionList.getAction().forEach(action -> System.out.printf("%s  \uD83D\uDD28 %s\n", nextPrefix, action.getName()));
                    }

                    var serviceStateTable = scpd.getServiceStateTable();
                    if (serviceStateTable != null) {
                        serviceStateTable.getStateVariable().forEach(stateVariable -> System.out.printf("%s  \uD83D\uDCC8 %s\n", nextPrefix, stateVariable.getName()));
                    }
                } catch (IOException ignored) {
                }
            });
        }

        var deviceList = device.getDeviceList();
        if (deviceList != null) {
            deviceList.getDevice().forEach(subDevice -> printIGDDeviceInfo(subDevice, client, nextPrefix));
        }
    }
}
