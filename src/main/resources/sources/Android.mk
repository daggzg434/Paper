LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := Armadillo
LOCAL_CPPFLAGS += -std=c++17
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cpp

LOCAL_SRC_FILES := $(wildcard $(LOCAL_PATH)/cpp/*.cpp) \
                   $(wildcard $(LOCAL_PATH)/cpp/*.hpp) \
				   $(wildcard $(LOCAL_PATH)/cpp/output/*.cpp) \
				   $(wildcard $(LOCAL_PATH)/cpp/output/*.hpp)

include $(BUILD_SHARED_LIBRARY)
