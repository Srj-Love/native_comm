import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:get/get.dart';

class HomeController extends GetxController {
  static const _methodChannel = const MethodChannel('technologies.apero/battery');
  static const _methodChannelHello = const MethodChannel('technologies.apero/hello');
  static const _eventChannel = const EventChannel('technologies.apero/event');
  static const _messageChannel = BasicMessageChannel('technologies.apero/message', StringCodec());

  StreamSubscription? _streamSubscription;


  var batteryLevel = 'Unknown battery level.'.obs;
  var eventReceived = 'No event.'.obs;
  var eventEnabled = false.obs;

  late TextEditingController messageController;
  var messageReceived = 'No message.'.obs;

  @override
  void onInit() {
    _methodChannelHello.setMethodCallHandler(_platformCallHandler);
    super.onInit();
    messageController = TextEditingController();
  }

  @override
  void onReady() {
    super.onReady();
  }

  @override
  void onClose() {}

  Future<dynamic> _platformCallHandler(MethodCall call) async {
    switch (call.method) {
      case "callMe":
        print('call callMe : arguments = ${call.arguments}');
        return Future.value('called from platform!');
      default:
        print('Unknowm method ${call.method}');
        throw MissingPluginException();
    }
  }

  void enableEventReceiver() {
    eventReceived.value = 'Checking data...';
    _streamSubscription = _eventChannel.receiveBroadcastStream().listen(
            (event) {
              eventEnabled(true);
              eventReceived.value = event;
          print('Received event: $event');
        },
        onError: (error) {
          print('Received error: ${error.message}');
        },
        cancelOnError: true);
  }

  void disableEventReceiver() {
    if (_streamSubscription != null) {
      _streamSubscription!.cancel();
      _streamSubscription = null;
      eventEnabled(false);
      eventReceived.value = 'No event.';
    }
  }

  void sendMessage() async {
    final String reply = (await _messageChannel.send(messageController.text))!;
    messageReceived.value = reply;
  }

  Future<void> getBatteryLevel() async {
    try {
      final int result = await _methodChannel.invokeMethod('getBatteryLevel');
      batteryLevel.value = '$result %';
    } on PlatformException catch (e) {
      batteryLevel.value = "Failed to get battery level: '${e.message}'.";
    }
  }
}
