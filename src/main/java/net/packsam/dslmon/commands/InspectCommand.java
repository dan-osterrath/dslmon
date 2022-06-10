package net.packsam.dslmon.commands;

import net.packsam.dslmon.DSLMonitor;
import net.packsam.dslmon.device.IGDClient;
import net.packsam.dslmon.device.TR64Client;
import net.packsam.dslmon.device.jaxb.tr64.device.Device;
import net.packsam.dslmon.device.jaxb.tr64.device.DeviceList;
import net.packsam.dslmon.device.jaxb.tr64.device.Service;
import net.packsam.dslmon.device.jaxb.tr64.device.ServiceList;
import net.packsam.dslmon.device.jaxb.tr64.service.*;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

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
        printDeviceInfo(
                tr64Device,
                "  ",
                Device::getManufacturer,
                Device::getFriendlyName,
                Device::getServiceList,
                ServiceList::getService,
                Service::getServiceType,
                service -> {
                    try {
                        return tr64Client.readServiceSpec(service);
                    } catch (IOException e) {
                        return null;
                    }
                },
                Scpd::getActionList,
                ActionList::getAction,
                Action::getName,
                Scpd::getServiceStateTable,
                ServiceStateTable::getStateVariable,
                StateVariable::getName,
                Device::getDeviceList,
                DeviceList::getDevice
        );

        var igdClient = new IGDClient(host, port);

        var igdDeviceSpec = igdClient.readDeviceSpec();
        var igdDevice = igdDeviceSpec.getDevice();
        System.out.println("Found IGD device:");
        printDeviceInfo(
                igdDevice,
                "  ",
                net.packsam.dslmon.device.jaxb.igd.device.Device::getManufacturer,
                net.packsam.dslmon.device.jaxb.igd.device.Device::getFriendlyName,
                net.packsam.dslmon.device.jaxb.igd.device.Device::getServiceList,
                net.packsam.dslmon.device.jaxb.igd.device.ServiceList::getService,
                net.packsam.dslmon.device.jaxb.igd.device.Service::getServiceType,
                service -> {
                    try {
                        return igdClient.readServiceSpec(service);
                    } catch (IOException e) {
                        return null;
                    }
                },
                net.packsam.dslmon.device.jaxb.igd.service.Scpd::getActionList,
                net.packsam.dslmon.device.jaxb.igd.service.ActionList::getAction,
                net.packsam.dslmon.device.jaxb.igd.service.Action::getName,
                net.packsam.dslmon.device.jaxb.igd.service.Scpd::getServiceStateTable,
                net.packsam.dslmon.device.jaxb.igd.service.ServiceStateTable::getStateVariable,
                net.packsam.dslmon.device.jaxb.igd.service.StateVariable::getName,
                net.packsam.dslmon.device.jaxb.igd.device.Device::getDeviceList,
                net.packsam.dslmon.device.jaxb.igd.device.DeviceList::getDevice
        );

        return 0;
    }

    private <DEVICE, DEVICE_LIST, SERVICE, SERVICE_LIST, ACTION, ACTION_LIST, SST, STATE_VARIABLE, SCPD> void printDeviceInfo(
            DEVICE device,
            String prefix,
            Function<DEVICE, String> deviceManufacturer,
            Function<DEVICE, String> deviceFriendlyName,
            Function<DEVICE, SERVICE_LIST> deviceServiceList,
            Function<SERVICE_LIST, List<SERVICE>> services,
            Function<SERVICE, String> serviceType,
            Function<SERVICE, SCPD> serviceSCPD,
            Function<SCPD, ACTION_LIST> scpdActionList,
            Function<ACTION_LIST, List<ACTION>> actions,
            Function<ACTION, String> actionName,
            Function<SCPD, SST> scpdServiceStateTable,
            Function<SST, List<STATE_VARIABLE>> stateVariables,
            Function<STATE_VARIABLE, String> stateVariableName,
            Function<DEVICE, DEVICE_LIST> deviceDeviceList,
            Function<DEVICE_LIST, List<DEVICE>> devices
    ) {
        var nextPrefix = prefix + "  ";

        System.out.printf("%s\uD83D\uDCE0 %s %s\n", prefix, deviceManufacturer.apply(device), deviceFriendlyName.apply(device));

        var serviceList = deviceServiceList.apply(device);
        if (serviceList != null) {
            services.apply(serviceList).forEach(service -> {
                System.out.printf("%s\uD83D\uDC81 %s\n", nextPrefix, serviceType.apply(service));

                var scpd = serviceSCPD.apply(service);
                if (scpd == null) {
                    return;
                }

                var actionList = scpdActionList.apply(scpd);
                if (actionList != null) {
                    actions.apply(actionList).forEach(action -> System.out.printf("%s  \uD83D\uDD28 %s\n", nextPrefix, actionName.apply(action)));
                }

                var serviceStateTable = scpdServiceStateTable.apply(scpd);
                if (serviceStateTable != null) {
                    stateVariables.apply(serviceStateTable).forEach(stateVariable -> System.out.printf("%s  \uD83D\uDCC8 %s\n", nextPrefix, stateVariableName.apply(stateVariable)));
                }
            });
        }

        var deviceList = deviceDeviceList.apply(device);
        if (deviceList != null) {
            devices.apply(deviceList).forEach(subDevice -> printDeviceInfo(
                    subDevice,
                    nextPrefix,
                    deviceManufacturer,
                    deviceFriendlyName,
                    deviceServiceList,
                    services,
                    serviceType,
                    serviceSCPD,
                    scpdActionList,
                    actions,
                    actionName,
                    scpdServiceStateTable,
                    stateVariables,
                    stateVariableName,
                    deviceDeviceList,
                    devices
            ));
        }
    }
}
