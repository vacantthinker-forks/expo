package abi42_0_0.host.exp.exponent.modules.api.components.gesturehandler.react;

import androidx.core.util.Pools;

import abi42_0_0.com.facebook.react.bridge.Arguments;
import abi42_0_0.com.facebook.react.bridge.WritableMap;
import abi42_0_0.com.facebook.react.uimanager.events.Event;
import abi42_0_0.com.facebook.react.uimanager.events.RCTEventEmitter;
import abi42_0_0.host.exp.exponent.modules.api.components.gesturehandler.GestureHandler;

import androidx.annotation.Nullable;

public class RNGestureHandlerEvent extends Event<RNGestureHandlerEvent> {

  public static final String EVENT_NAME = "onGestureHandlerEvent";

  private static final int TOUCH_EVENTS_POOL_SIZE = 7; // magic

  private static final Pools.SynchronizedPool<RNGestureHandlerEvent> EVENTS_POOL =
          new Pools.SynchronizedPool<>(TOUCH_EVENTS_POOL_SIZE);

  public static RNGestureHandlerEvent obtain(
          GestureHandler handler,
          @Nullable RNGestureHandlerEventDataExtractor dataExtractor) {
    RNGestureHandlerEvent event = EVENTS_POOL.acquire();
    if (event == null) {
      event = new RNGestureHandlerEvent();
    }
    event.init(handler, dataExtractor);
    return event;
  }

  private WritableMap mExtraData;
  private short mCoalescingKey;

  private RNGestureHandlerEvent() {
  }

  private void init(
          GestureHandler handler,
          @Nullable RNGestureHandlerEventDataExtractor dataExtractor) {
    super.init(handler.getView().getId());
    mExtraData = Arguments.createMap();
    if (dataExtractor != null) {
      dataExtractor.extractEventData(handler, mExtraData);
    }
    mExtraData.putInt("handlerTag", handler.getTag());
    mExtraData.putInt("state", handler.getState());
    mCoalescingKey = handler.getEventCoalescingKey();
  }

  @Override
  public void onDispose() {
    mExtraData = null;
    EVENTS_POOL.release(this);
  }

  @Override
  public String getEventName() {
    return EVENT_NAME;
  }

  @Override
  public boolean canCoalesce() {
    return true;
  }

  @Override
  public short getCoalescingKey() {
    return mCoalescingKey;
  }

  @Override
  public void dispatch(RCTEventEmitter rctEventEmitter) {
    rctEventEmitter.receiveEvent(getViewTag(), EVENT_NAME, mExtraData);
  }
}
