package comshiji.testpoynt;

import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.poynt.os.model.Payment;
import co.poynt.os.util.StringUtil;

/**
 * @author Stones.Tan
 * @date 10:08
 */
public class PaymentUtil {
    private Map<String,List<Payment>> stringListMap = new HashMap<>();

    private static PaymentUtil paymentUtil;

    private PaymentUtil(){}
    public static PaymentUtil getPaymentUtil(){
        if(paymentUtil==null){
            synchronized (PaymentUtil.class){
                if(paymentUtil==null){
                    paymentUtil=new PaymentUtil();
                }
            }
        }
        return paymentUtil;
    }


    public void putPayment(Payment payment,String oId){
        List<Payment> list = stringListMap.get(oId);
        if(list==null){
            list = new ArrayList<>();
        }
        list.add(payment);
        stringListMap.put(oId,list);
    }

    public Payment getPayment(String orID){
        if(!StringUtil.isEmpty(orID)){
            List<Payment> payments = stringListMap.get(orID);
            if(!payments.isEmpty()){
                return payments.get(payments.size()-1);
            }
        }
        return null;
    }

    public List<String> getKey(){
        List<String> strings = new ArrayList<>();
        for (String key : stringListMap.keySet()) {
            strings.add(key);
        }
        return strings;
    }

    public void saveAll(){
        Gson gson = new Gson();
        String json= gson.toJson(stringListMap);
        FileUtil.saveTransactionContent(json);
    }
}
