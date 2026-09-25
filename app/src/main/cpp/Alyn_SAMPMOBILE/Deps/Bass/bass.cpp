#include <dlfcn.h>

#include "bass.h"
#include "../../Client.h"

int (*BASS_Init) (uint32_t, uint32_t, uint32_t);
int (*BASS_Free) (void);
int (*BASS_SetConfigPtr) (uint32_t, const char*);
int (*BASS_SetConfig) (uint32_t, uint32_t);
int (*BASS_ChannelStop) (uint32_t);
int (*BASS_StreamCreateURL) (char*, uint32_t, uint32_t, uint32_t);
int (*BASS_StreamCreate) (uint32_t, uint32_t, uint32_t, STREAMPROC*, void*);
int (*BASS_ChannelPlay) (uint32_t, bool);
int (*BASS_ChannelPause) (uint32_t);
int* BASS_ChannelGetTags;
int* BASS_ChannelSetSync;
int* BASS_StreamGetFilePosition;
int (*BASS_StreamFree) (uint32_t);
int (*BASS_ErrorGetCode) (void);
int (*BASS_Set3DFactors) (float, float, float);
int (*BASS_Set3DPosition) (const BASS_3DVECTOR*, const BASS_3DVECTOR*, const BASS_3DVECTOR*, const BASS_3DVECTOR*);
int (*BASS_Apply3D) (void);
int (*BASS_ChannelSetFX) (uint32_t, HFX);
int (*BASS_ChannelRemoveFX) (uint32_t, HFX);
int (*BASS_FXSetParameters) (HFX, const void*);
int (*BASS_IsStarted) (void);
int (*BASS_RecordGetDeviceInfo) (uint32_t, BASS_DEVICEINFO*);
int (*BASS_RecordInit) (int);
int (*BASS_RecordGetDevice) (void);
int (*BASS_RecordFree) (void);
int (*BASS_RecordStart) (uint32_t, uint32_t, uint32_t, RECORDPROC*, void*);
int (*BASS_ChannelSetAttribute) (uint32_t, uint32_t, float);
int (*BASS_ChannelGetData) (uint32_t, void*, uint32_t);
int (*BASS_RecordSetInput) (int, uint32_t, float);
int (*BASS_StreamPutData) (uint32_t, const void*, uint32_t);
int (*BASS_ChannelSetPosition) (uint32_t, uint64_t, uint32_t);
int (*BASS_ChannelIsActive) (uint32_t);
int (*BASS_ChannelSlideAttribute) (uint32_t, uint32_t, float, uint32_t);
int (*BASS_ChannelSet3DAttributes) (uint32_t, int, float, float, int, int, float);
int (*BASS_ChannelSet3DPosition) (uint32_t, const BASS_3DVECTOR*, const BASS_3DVECTOR*, const BASS_3DVECTOR*);
int (*BASS_SetVolume) (float);

bool LoadBassLibrary()
{
	spdlog::info("Loading BASS library..");
	void* v0 = dlopen("libbass.so", RTLD_NOW | RTLD_LOCAL);

	if (!v0) {
		const char* error = dlerror();
		spdlog::error("Failed to load libbass.so: {}", error ? error : "unknown linker error");
		return false;
	}

#define LOAD_BASS_SYMBOL(symbol) \
	symbol = reinterpret_cast<decltype(symbol)>(dlsym(v0, #symbol))

	LOAD_BASS_SYMBOL(BASS_Init);
	LOAD_BASS_SYMBOL(BASS_Free);
	LOAD_BASS_SYMBOL(BASS_SetConfigPtr);
	LOAD_BASS_SYMBOL(BASS_SetConfig);
	LOAD_BASS_SYMBOL(BASS_ChannelStop);
	LOAD_BASS_SYMBOL(BASS_StreamCreateURL);
	LOAD_BASS_SYMBOL(BASS_StreamCreate);
	LOAD_BASS_SYMBOL(BASS_ChannelPlay);
	LOAD_BASS_SYMBOL(BASS_ChannelPause);
	LOAD_BASS_SYMBOL(BASS_ChannelGetTags);
	LOAD_BASS_SYMBOL(BASS_ChannelSetSync);
	LOAD_BASS_SYMBOL(BASS_StreamGetFilePosition);
	LOAD_BASS_SYMBOL(BASS_StreamFree);
	LOAD_BASS_SYMBOL(BASS_ErrorGetCode);
	LOAD_BASS_SYMBOL(BASS_Set3DFactors);
	LOAD_BASS_SYMBOL(BASS_Set3DPosition);
	LOAD_BASS_SYMBOL(BASS_Apply3D);
	LOAD_BASS_SYMBOL(BASS_ChannelSetFX);
	LOAD_BASS_SYMBOL(BASS_ChannelRemoveFX);
	LOAD_BASS_SYMBOL(BASS_FXSetParameters);
	LOAD_BASS_SYMBOL(BASS_IsStarted);
	LOAD_BASS_SYMBOL(BASS_RecordGetDeviceInfo);
	LOAD_BASS_SYMBOL(BASS_RecordInit);
	LOAD_BASS_SYMBOL(BASS_RecordGetDevice);
	LOAD_BASS_SYMBOL(BASS_RecordFree);
	LOAD_BASS_SYMBOL(BASS_RecordStart);
	LOAD_BASS_SYMBOL(BASS_ChannelSetAttribute);
	LOAD_BASS_SYMBOL(BASS_ChannelGetData);
	LOAD_BASS_SYMBOL(BASS_RecordSetInput);
	LOAD_BASS_SYMBOL(BASS_StreamPutData);
	LOAD_BASS_SYMBOL(BASS_ChannelSetPosition);
	LOAD_BASS_SYMBOL(BASS_ChannelIsActive);
	LOAD_BASS_SYMBOL(BASS_ChannelSlideAttribute);
	LOAD_BASS_SYMBOL(BASS_ChannelSet3DAttributes);
	LOAD_BASS_SYMBOL(BASS_ChannelSet3DPosition);
	LOAD_BASS_SYMBOL(BASS_SetVolume);

#undef LOAD_BASS_SYMBOL

	if (!BASS_Init || !BASS_Free || !BASS_SetConfigPtr || !BASS_SetConfig ||
		!BASS_ChannelStop || !BASS_StreamCreateURL || !BASS_StreamCreate ||
		!BASS_ChannelPlay || !BASS_ChannelPause || !BASS_ChannelGetTags ||
		!BASS_ChannelSetSync || !BASS_StreamGetFilePosition || !BASS_StreamFree ||
		!BASS_ErrorGetCode || !BASS_Set3DFactors || !BASS_Set3DPosition ||
		!BASS_Apply3D || !BASS_ChannelSetFX || !BASS_ChannelRemoveFX ||
		!BASS_FXSetParameters || !BASS_IsStarted || !BASS_RecordGetDeviceInfo ||
		!BASS_RecordInit || !BASS_RecordGetDevice || !BASS_RecordFree ||
		!BASS_RecordStart || !BASS_ChannelSetAttribute ||
		!BASS_ChannelGetData || !BASS_RecordSetInput || !BASS_StreamPutData ||
		!BASS_ChannelSetPosition || !BASS_ChannelIsActive ||
		!BASS_ChannelSlideAttribute || !BASS_ChannelSet3DAttributes ||
		!BASS_ChannelSet3DPosition || !BASS_SetVolume) {
		spdlog::error("libbass.so is missing one or more required exports");
		return false;
	}

	spdlog::info("BASS library loaded successfully");
	return true;
}
