package org.example;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Main {
    public static void main(String[] args) {

        String json = "{\"description\": { \"participantInn\": \"string\" }, \"doc_id\": \"string\", \"doc_status\": \"string\", \"doc_type\": \"LP_INTRODUCE_GOODS\", \"importRequest\": true, \"owner_inn\": \"string\", \"participant_inn\": \"string\", \"producer_inn\": \"string\", \"production_date\": \"2020-01-23\", \"production_type\": \"string\", \"products\": [ { \"certificate_document\": \"string\", \"certificate_document_date\": \"2020-01-23\", \"certificate_document_number\": \"string\", \"owner_inn\": \"string\", \"producer_inn\": \"string\", \"production_date\": \"2020-01-23\", \"tnved_code\": \"string\", \"uit_code\": \"string\", \"uitu_code\": \"string\" } ], \"reg_date\": \"2020-01-23\", \"reg_number\": \"string\"}";
        Gson gson = new Gson();
        CrptApi.CrptDocument crptDocument = gson.fromJson(json, CrptApi.CrptDocument.class);
        System.out.println(crptDocument.toString());

        CrptApi crptApi=new CrptApi(TimeUnit.SECONDS,2);
        crptApi.createDocument(crptDocument,"signarue");


    }

    public static class CrptApi {
        private final int requestLimit;
        private final TimeUnit timeUnit;
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        private final AtomicInteger requestCounter = new AtomicInteger(0);
        private final Object lock = new Object();

        public CrptApi(TimeUnit timeUnit, int requestLimit) {
            this.timeUnit = timeUnit;
            this.requestLimit = requestLimit;
            scheduler.scheduleAtFixedRate(() -> requestCounter.set(0), 0, 1, timeUnit);
        }

        public void createDocument(CrptDocument document, String signature) {
            synchronized (lock) {
                if (requestCounter.getAndIncrement() >= requestLimit) {
                    return;
                }
            }

            sendDocument( document, signature);
        }
        public void sendDocument( CrptDocument document, String signature) {
            String url="https://ismp.crpt.ru/api/v3/lk/documents/create";
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);

            Gson gson = new Gson();
            String json = gson.toJson(document); // Преобразование объекта в JSON

            try {
                StringEntity entity = new StringEntity(json);
                httpPost.setEntity(entity);
                httpPost.setHeader("Content-Type", "application/json");

                // Опционально, если необходимо передать документ и подпись
                httpPost.setHeader("Document", "document");
                httpPost.setHeader("Signature", signature);

                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    String responseString = EntityUtils.toString(responseEntity);
                    System.out.println(responseString); // Обработка ответа, если необходимо
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public class CrptDocument {
            private Description description;
            private String doc_id;
            private String doc_status;
            private String doc_type;
            private boolean importRequest;
            private String owner_inn;
            private String participant_inn;
            private String producer_inn;
            private String production_date;

            @Override
            public String toString() {
                return "CrptDocument{" +
                        "description=" + description.toString() +
                        ", doc_id='" + doc_id + '\'' +
                        ", doc_status='" + doc_status + '\'' +
                        ", doc_type='" + doc_type + '\'' +
                        ", importRequest=" + importRequest +
                        ", owner_inn='" + owner_inn + '\'' +
                        ", participant_inn='" + participant_inn + '\'' +
                        ", producer_inn='" + producer_inn + '\'' +
                        ", production_date='" + production_date + '\'' +
                        ", production_type='" + production_type + '\'' +
                        ", products=" + products.toString() +
                        ", reg_date='" + reg_date + '\'' +
                        ", reg_number='" + reg_number + '\'' +
                        '}';
            }

            private String production_type;
            private List<Product> products;
            private String reg_date;
            private String reg_number;




            public static class Description {
                private String participantInn;


                @Override
                public String toString() {
                    return "Description{" +
                            "participantInn='" + participantInn + '\'' +
                            '}';
                }
            }

            public static class Product {
                @Override
                public String toString() {
                    return "Product{" +
                            "certificate_document='" + certificate_document + '\'' +
                            ", certificate_document_date='" + certificate_document_date + '\'' +
                            ", certificate_document_number='" + certificate_document_number + '\'' +
                            ", owner_inn='" + owner_inn + '\'' +
                            ", producer_inn='" + producer_inn + '\'' +
                            ", production_date='" + production_date + '\'' +
                            ", tnved_code='" + tnved_code + '\'' +
                            ", uit_code='" + uit_code + '\'' +
                            ", uitu_code='" + uitu_code + '\'' +
                            '}';
                }

                private String certificate_document;
                private String certificate_document_date;
                private String certificate_document_number;
                private String owner_inn;
                private String producer_inn;
                private String production_date;
                private String tnved_code;
                private String uit_code;
                private String uitu_code;


            }
        }
    }
}

