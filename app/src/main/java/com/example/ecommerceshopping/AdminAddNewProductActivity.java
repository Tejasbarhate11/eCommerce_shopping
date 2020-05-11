package com.example.ecommerceshopping;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import javax.xml.transform.Result;

public class AdminAddNewProductActivity extends AppCompatActivity {
    private String categoryName;
    private TextView category;
    private ProgressDialog progressDialog;
    private String saveDate, saveTime, productRandomKey;
    private String productName, productDescription, productPrice;

    private Button addProductbtn;
    private EditText inputProductName, inputProductDescription, inputProductPrice;
    private ImageView productImage;

    private static final int GALLERYINTENT = 1;

    private Uri imageUri;
    private DatabaseReference databaseReference;

    private StorageReference reference;

    private String downloadImageUrl;
    private UploadTask uploadTask;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_product);

        categoryName = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("category")).toString();
        category = (TextView) findViewById(R.id.categry_name);
        category.setText(categoryName);

        addProductbtn = (Button) findViewById(R.id.add_product_btn);
        inputProductName = (EditText) findViewById(R.id.product_name);
        inputProductDescription = (EditText) findViewById(R.id.product_description);
        inputProductPrice = (EditText) findViewById(R.id.product_price);
        productImage = (ImageView) findViewById(R.id.select_product_image);

        progressDialog =new ProgressDialog(this);
        reference = FirebaseStorage.getInstance().getReference().child("Product images");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Products");
        productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        addProductbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateProductdata();
            }
        });


    }
    //opens gallery to pick images
    private void OpenGallery(){
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GALLERYINTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERYINTENT && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            imageUri = data.getData();
            productImage.setImageURI(imageUri);
            System.out.println("set image uri done");
        }
    }

    private void ValidateProductdata(){


        productName = inputProductName.getText().toString();
        productDescription = inputProductDescription.getText().toString();
        productPrice = inputProductPrice.getText().toString();

        if(TextUtils.isEmpty(productName)){
            Toast.makeText(this, "Enter the Product Name", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(productDescription)){
            Toast.makeText(this, "Enter the Product Description", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(productPrice)){
            Toast.makeText(this, "Enter the Product Price", Toast.LENGTH_SHORT).show();
        }else if (imageUri == null){
            Toast.makeText(this, "Product image is mandatory", Toast.LENGTH_SHORT).show();
        }else{
            System.out.println("StoreProductInformantion been called");
            StoreProductInformation();
        }
    }
    private void StoreProductInformation(){
        progressDialog.setTitle("Adding Product");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentdate = new SimpleDateFormat("MMM dd, yyyy");
        saveDate = currentdate.format(calendar.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveTime = currentTime.format(calendar.getTime());

        productRandomKey = saveDate + saveTime;
        final StorageReference imageReference = reference.child(imageUri.getLastPathSegment()+" "+productRandomKey+".jpeg");
        uploadTask = imageReference.putFile(imageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                String message = e.getMessage();
                Toast.makeText(AdminAddNewProductActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AdminAddNewProductActivity.this, "Product Image Uploaded Successfully...", Toast.LENGTH_SHORT).show();
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            progressDialog.dismiss();
                            throw task.getException();

                        }
                        downloadImageUrl = imageReference.getDownloadUrl().toString();

                        return imageReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            downloadImageUrl = task.getResult().toString();

                            SaveProductInfotoDatabase();
                        }
                    }
                });
            }
        });

    }

    private void SaveProductInfotoDatabase(){
        final HashMap<String,Object> productMap = new HashMap<>();
        productMap.put("pid",productRandomKey);
        productMap.put("date",saveDate);
        productMap.put("time",saveDate);
        productMap.put("description",productDescription);
        productMap.put("name",productName);
        productMap.put("price",productPrice);
        productMap.put("image",downloadImageUrl);
        databaseReference.child(productRandomKey).updateChildren(productMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            Toast.makeText(AdminAddNewProductActivity.this, "Product Added Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AdminAddNewProductActivity.this,AdminCategoryActivity.class));
                        }else{
                            String message = task.getException().toString();
                            progressDialog.dismiss();
                            Toast.makeText(AdminAddNewProductActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
