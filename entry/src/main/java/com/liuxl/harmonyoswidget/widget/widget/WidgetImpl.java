package com.liuxl.harmonyoswidget.widget.widget;

import com.dingmouren.paletteimageview.Palette;
import com.liuxl.harmonyoswidget.MainAbility;
import com.liuxl.harmonyoswidget.ResourceTable;
import com.liuxl.harmonyoswidget.widget.controller.FormController;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.FormBindingData;
import ohos.aafwk.ability.ProviderFormInfo;
import ohos.aafwk.content.Intent;
import ohos.app.Context;
import ohos.app.Environment;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.global.resource.NotExistException;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.ImagePacker;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.media.image.common.AlphaType;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Position;
import ohos.media.image.common.Size;
import ohos.utils.zson.ZSONObject;

import java.io.*;
import java.nio.ByteBuffer;

import static com.dingmouren.paletteimageview.util.ColorUtil.*;

public class WidgetImpl extends FormController {

    private static final HiLogLabel TAG = new HiLogLabel(HiLog.DEBUG, 0x0, "liuxl.WidgetImpl");
    private Palette mPalette;

    public WidgetImpl(Context context, String formName, Integer dimension) {
        super(context, formName, dimension);
    }

    @Override
    public ProviderFormInfo bindFormData() {
        HiLog.debug(TAG, "bindFormData() called, dimension: " + dimension);
        if (dimension == 2) {
            //2*2卡片

        } else if (dimension == 3) {
            //2*4卡片
            InputStream inputStream = null;
            ImageSource imageSource = null;
            PixelMap pixelMap = null;
            try {
                inputStream = context.getResourceManager().getResource(ResourceTable.Media_godfather3);
                imageSource = ImageSource.create(inputStream, null);
                pixelMap = imageSource.createPixelmap(null);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NotExistException e) {
                e.printStackTrace();
            }

            if (pixelMap != null) {
                long timestamp = System.currentTimeMillis();
                initPalette(pixelMap);
                int dominantColor = mPalette.getDominantSwatch().getRgb();
                ZSONObject zsonObject = new ZSONObject();
                zsonObject.put("backgroundColor", dominantColor);
                zsonObject.put("title", "教父");
                zsonObject.put("detail", "永远不要让你的对手知道你在想什么。");
                zsonObject.put("imageSrc", "memory://bg" + timestamp + ".jpeg");
                FormBindingData formBindingData = new FormBindingData(zsonObject);
                PixelMap resultPixelMap = getImageToChange(pixelMap);
//                savePixelMap(resultPixelMap, "test.png");
                Size size = resultPixelMap.getImageInfo().size;

//                ByteBuffer byteBuffer = ByteBuffer.allocate(size.width * size.height * 4);
//                resultPixelMap.readPixels(byteBuffer);
//                byteBuffer.flip();
//                byte[] bytes = new byte[byteBuffer.capacity()];
//                byteBuffer.get(bytes);

                byte[] bytes = new byte[size.width * size.height * 4];
                saveBytes(bytes, resultPixelMap);

                formBindingData.addImageData("bg" + timestamp + ".jpeg", bytes);
                formBindingData.updateData(zsonObject);

                ProviderFormInfo formInfo = new ProviderFormInfo();
                formInfo.setJsBindingData(formBindingData);
                return formInfo;
            }

        }
        return null;
    }

    @Override
    public void updateFormData(long formId, Object... vars) {
    }

    @Override
    public void onTriggerFormEvent(long formId, String message) {
    }

    @Override
    public Class<? extends AbilitySlice> getRoutePageSlice(Intent intent) {
        return null;
    }

    private void initPalette(PixelMap pixelMap) {
        Palette.Builder builder = Palette.from(pixelMap);
        mPalette = builder.generate(new EventHandler(EventRunner.getMainEventRunner()));
    }

    private PixelMap getImageToChange(PixelMap pixelMap) {
        PixelMap.InitializationOptions initializationOptions = new PixelMap.InitializationOptions();
        int width = pixelMap.getImageInfo().size.width;
        int height = pixelMap.getImageInfo().size.height;
        initializationOptions.size = new Size(width, height);
        initializationOptions.pixelFormat = PixelFormat.ARGB_8888;
        initializationOptions.editable = true;
        PixelMap resultPixelMap = PixelMap.create(pixelMap, initializationOptions);
        resultPixelMap.setAlphaType(AlphaType.PREMUL);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int color = pixelMap.readPixel(new Position(i, j));
                int g = green(color);
                int r = red(color);
                int b = blue(color);
                int a = alpha(color);

                if (i < width / 2) {
                    float temp = 255 * 2f * i / width;
                    a = (int) temp;
                }

                color = argb(a, r, g, b);
                resultPixelMap.writePixel(new Position(i, j), color);
            }
        }
        return resultPixelMap;
    }

    private long savePixelMap(PixelMap pixelMap, String fileName){
        ImagePacker imagePacker = ImagePacker.create();
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ImagePacker.PackingOptions packingOptions = new ImagePacker.PackingOptions();
        packingOptions.format = "image/jpeg";
        packingOptions.quality = 100;
        boolean result = imagePacker.initializePacking(outputStream, packingOptions);
        result = imagePacker.addImage(pixelMap);
        return imagePacker.finalizePacking();
    }

    private long saveBytes(byte[] bytes, PixelMap pixelMap) {
        ImagePacker imagePacker = ImagePacker.create();
        ImagePacker.PackingOptions packingOptions = new ImagePacker.PackingOptions();

        packingOptions.format = "image/jpeg";
        packingOptions.quality = 100;

        boolean result = imagePacker.initializePacking(bytes, packingOptions);
        HiLog.debug(TAG, "initializePacking result: " + result);
        result = imagePacker.addImage(pixelMap);
        HiLog.debug(TAG, "addImage result: " + result);

        return imagePacker.finalizePacking();
    }
}
