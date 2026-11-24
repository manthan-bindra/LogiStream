package com.logistream.app;

import com.logistream.concurrent.DeliverySimulator;
import com.logistream.model.*;
import com.logistream.repository.*;
import com.logistream.service.*;
import com.logistream.util.LogiStreamUtil;

/** Builds the application object graph. Phase 1 uses in-memory repositories. */
public class AppContext {
 private final MemoryRepository<Parcel> parcelRepo=new MemoryRepository<Parcel>();
 private final MemoryRepository<Driver> driverRepo=new MemoryRepository<Driver>();
 private final MemoryRepository<Vehicle> vehicleRepo=new MemoryRepository<Vehicle>();
 private final MemoryRepository<Customer> customerRepo=new MemoryRepository<Customer>();
 private final MemoryRepository<Route> routeRepo=new MemoryRepository<Route>();
 private final MemoryRepository<Shift> shiftRepo=new MemoryRepository<Shift>();
 private final AuditLogger audit=new AuditLogger();
 private final VehicleService vehicleService=new VehicleService(vehicleRepo,audit);
 private final DriverService driverService=new DriverService(driverRepo,vehicleService,audit);
 private final CustomerService customerService=new CustomerService(customerRepo,parcelRepo,audit);
 private final ParcelService parcelService=new ParcelService(parcelRepo,customerRepo,audit);
 private final ShiftService shiftService=new ShiftService(shiftRepo,driverService,audit);
 private final RouteService routeService=new RouteService(routeRepo,parcelService,audit);
 private final DispatchService dispatchService=new DispatchService(parcelService,driverService,routeService,audit);
 private final DeliverySimulator simulator=new DeliverySimulator(routeService,parcelService,driverService);
 private final ReportService reportService=new ReportService(parcelService,driverService,shiftService);
 public AuditLogger audit(){return audit;} public VehicleService vehicles(){return vehicleService;}
 public DriverService drivers(){return driverService;} public CustomerService customers(){return customerService;}
 public ParcelService parcels(){return parcelService;} public ShiftService shifts(){return shiftService;}
 public RouteService routes(){return routeService;} public DispatchService dispatch(){return dispatchService;}
 public DeliverySimulator simulator(){return simulator;} public ReportService reports(){return reportService;}

 public void loadDemoData() {
     if (!customerService.listAll().isEmpty() || !driverService.listAll().isEmpty()) {
         LogiStreamUtil.info("Demo data is only loaded into an empty system (existing records found).");
         return;
     }
     String[][] sampleCustomers = {
         {"Asha Rao", "9876500001", "12 Lake Road", "North"},
         {"Ravi Kumar", "9876500002", "7 Mill Street", "South"},
         {"Meena Iyer", "9876500003", "45 Park Avenue", "East"},
         {"Imran Shaikh", "9876500004", "3 Harbour Lane", "West"}
     };
     for (String[] c : sampleCustomers) customerService.add(c[0], c[1], c[2], c[3]);

     Vehicle bike = vehicleService.add("BIKE", "KA01-AB-1001");
     Vehicle van1 = vehicleService.add("VAN", "KA01-CD-2002");
     Vehicle van2 = vehicleService.add("VAN", "KA01-EF-3003");
     Driver d1 = driverService.add("Sunil", "9000000001", "North");
     Driver d2 = driverService.add("Priya", "9000000002", "South");
     Driver d3 = driverService.add("Vikram", "9000000003", "East");
     driverService.assignVehicle(d1.getId(), bike.getId());
     driverService.assignVehicle(d2.getId(), van1.getId());
     driverService.assignVehicle(d3.getId(), van2.getId());

     parcelService.register("EXPRESS", "CUS0001", "North", 3.5);
     parcelService.register("STANDARD", "CUS0002", "South", 12.0);
     parcelService.register("FRAGILE", "CUS0003", "East", 6.0, 8000);
     parcelService.register("STANDARD", "CUS0004", "West", 25.0);
     parcelService.register("EXPRESS", "CUS0002", "South", 2.0);
     parcelService.register("STANDARD", "CUS0001", "North", 18.0);
     LogiStreamUtil.success("Demo data loaded: 4 customers, 3 vehicles, 3 drivers, 6 parcels.");
     LogiStreamUtil.info("Next: start driver shifts, dispatch pending parcels, then run deliveries.");
 }
}
