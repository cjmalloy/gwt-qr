package com.cjmalloy.qr.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Image;


public class Qr
{
    private static final String QR_JS_SCRIPT = GWT.getModuleBaseForStaticFiles() + "jsqrcode.js";

    private static Qr instance = null;

    protected Qr() {}

    public String decode(Canvas canvas)
    {
        return decode(canvas.getCanvasElement());
    }

    public native String decode(CanvasElement canvas_qr) /*-{

        var context = canvas_qr.getContext('2d');
        qrcode.width = canvas_qr.width;
        qrcode.height = canvas_qr.height;
        qrcode.imagedata = context.getImageData(0, 0, qrcode.width, qrcode.height);
        qrcode.result = qrcode.process(context);
        return qrcode.result;
    }-*/;

    public native String decode(ImageElement image) throws JavaScriptException /*-{

        var canvas_qr = document.createElement('canvas');
        var context = canvas_qr.getContext('2d');
        var nheight = image.height;
        var nwidth = image.width;
        if(image.width*image.height>qrcode.maxImgSize)
        {
            var ir = image.width / image.height;
            nheight = Math.sqrt(qrcode.maxImgSize/ir);
            nwidth=ir*nheight;
        }

        canvas_qr.width = nwidth;
        canvas_qr.height = nheight;

        context.drawImage(image, 0, 0, canvas_qr.width, canvas_qr.height );
        qrcode.width = canvas_qr.width;
        qrcode.height = canvas_qr.height;
        qrcode.imagedata = context.getImageData(0, 0, canvas_qr.width, canvas_qr.height);
        qrcode.result = qrcode.process(context);
        return qrcode.result;
    }-*/;

    public void decode(String src, final Callback<String, Exception> callback)
    {
        final Image image = new Image();
        image.addErrorHandler(new ErrorHandler()
        {
            @Override
            public void onError(ErrorEvent event)
            {
                callback.onFailure(new Exception("Error loading image"));
            }
        });
        image.addLoadHandler(new LoadHandler()
        {
            @Override
            public void onLoad(LoadEvent event)
            {
                try
                {
                    String result = decode(ImageElement.as(image.getElement()));
                    callback.onSuccess(result);
                }
                catch (JavaScriptException e)
                {
                    callback.onFailure(e);
                }
            }
        });
        image.setUrl(src);
    }

    protected void load(final Callback<Qr, Exception> callback)
    {
        ScriptInjector.fromUrl(QR_JS_SCRIPT).setCallback(new Callback<Void, Exception>()
        {
            @Override
            public void onFailure(Exception reason)
            {
                callback.onFailure(reason);
            }

            @Override
            public void onSuccess(Void result)
            {
                callback.onSuccess(instance);
            }
        }).inject();
    }

    public static void get(Callback<Qr, Exception> callback)
    {
        if (instance == null)
        {
            instance = GWT.create(Qr.class);
            instance.load(callback);
        }
        else
        {
            callback.onFailure(new Exception("Already loaded"));
        }
    }
}
