package ping.com.nfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcF;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

public class MainActivity extends AppCompatActivity {

    private TextView mTextView;
    private NfcAdapter mNfcAdapter;
    private PendingIntent pendingIntent;
    private Observable<String> observable;
    private Subscriber<String> subscriber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.tv);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            ToastUtil.showTextToast(this, "手机不支持NFC功能");
            finish();
            return;
        }

        if (!mNfcAdapter.isEnabled()) {
            mTextView.setText("尚未开启nfc");
            ToastUtil.showTextToast(this, "尚未开启nfc");
        } else {
            mTextView.setText("扫描中。。");
            ToastUtil.showTextToast(this, "开启nfc成功");
        }

        onNewIntent(getIntent());
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        JLog.i("收到了");
        Parcelable p = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        JLog.i("p:" + p);
        JLog.i("getIntent().getAction()):" + getIntent().getAction());
        if (p != null) {
            processIntent(intent);
        }
    }

    private void processIntent(Intent intent) {
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);


        for (String tech :
                tagFromIntent.getTechList()) {
            JLog.i("tech:" + tech);
        }

        IsoDep mfc = IsoDep.get(tagFromIntent);


        try {
            mfc.connect();
            JLog.i("--------------开始读卡---------------");


            byte[] transceive = mfc.transceive(hexString2Bytes("00A404000E325041592E5359532E4444463031"));
            JLog.i(bytesToHexString(transceive));
            mTextView.append("\n" + bytesToHexString(transceive));

            subscriber = new Subscriber<String>() {

                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(String s) {

                }
            };

            observable = Observable.create(new Observable.OnSubscribe<String>() {

                @Override
                public void call(Subscriber<? super String> subscriber) {
                        subscriber.onNext("");
                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    //字符序列转换为16进制字符串
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    //将16进制字符串转换成字节数组
    public static byte[] hexString2Bytes(String data) {
        if (data == null)
            return null;
        byte[] result = new byte[(data.length() + 1) / 2];
        if ((data.length() & 1) == 1) {
            data += "0";
        }
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (hex2byte(data.charAt(i * 2 + 1)) | (hex2byte(data.charAt(i * 2)) << 4));
        }
        return result;
    }

    public static byte hex2byte(char hex) {
        if (hex <= 'f' && hex >= 'a') {
            return (byte) (hex - 'a' + 10);
        }

        if (hex <= 'F' && hex >= 'A') {
            return (byte) (hex - 'A' + 10);
        }

        if (hex <= '9' && hex >= '0') {
            return (byte) (hex - '0');
        }

        return 0;
    }


}
