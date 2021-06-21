import 'package:flutter/material.dart';
import 'package:get/get.dart';

import '../controllers/home_controller.dart';

class HomeView extends GetView<HomeController> {



  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Flutter Native Communication'),
        centerTitle: true,
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ListTile(
              title: Text('Flutter Calls Native'),
              subtitle: Text('Method Channel: tap to view'),
              isThreeLine: true,
              leading: Icon(Icons.battery_unknown_rounded),
              trailing: Obx(() => Text(controller.batteryLevel.value)),
              onTap: () => controller.getBatteryLevel(),
            ),
            ListTile(
              title: Text('Native calls Flutter'),
              subtitle: Text('Method Channel: tap to view'),
              isThreeLine: true,
              leading: Icon(Icons.battery_unknown_rounded),
              trailing: Obx(() => Text(controller.batteryLevel.value)),
              onTap: (){},
            ),
            Obx(() =>ListTile(
              title: Text('Event Channel'),
              subtitle: Text( 'Enabled: ${controller.eventEnabled.value}'),
              isThreeLine: true,
              leading: Icon(Icons.event),
              trailing: Obx(() => Text(controller.eventReceived.value)),
              onTap: (){
                if(controller.eventEnabled.value)
                controller.disableEventReceiver();
                else controller.enableEventReceiver();
              },
            )),
            Obx(() =>ListTile(
              title: Text('Basic Channel'),
              subtitle: Text( 'Enabled: ${controller.messageReceived.value}'),
              isThreeLine: true,
              leading: Icon(Icons.message),
              onTap: (){
                showDialogEdit();
              },
            )),
            // TextButton(onPressed: (){}, style: buildStyleFrom(), child: Text('Basic Message Channel')),
          ],
        ),
      ),
    );
  }

  ButtonStyle buildStyleFrom() {
    return TextButton.styleFrom(
                primary: Colors.white,
                backgroundColor: Colors.teal,
                onSurface: Colors.grey,
              );
  }

  void showDialogEdit() {
    Get.defaultDialog(
        title: '',
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: controller.messageController,
              keyboardType: TextInputType.text,
              maxLines: 1,
              decoration: InputDecoration(
                  labelText: 'Send native data',
                  hintMaxLines: 1,
                  border: OutlineInputBorder(
                      borderSide: BorderSide(color: Colors.green, width: 4.0))),
            ),
            SizedBox(height: 30.0),
            TextButton(onPressed: (){
              if (controller.messageController.text.isNotEmpty) {
                controller.sendMessage();
                Get.back();
              } else {
                Get.snackbar('Error', 'Please Enter data');
              }
            }, style: buildStyleFrom(), child: Text('Send Data')),
          ],
        ),
        radius: 10.0);
  }
}
