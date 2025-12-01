package com.darwin.cloudfile;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;

public class FilesFragment extends Fragment implements FtpFileAdapter.OnItemClickListener{
    String dir="/";
    VModel model;
    String[] ftpLogin;
    ArrayList<FtpFileItem> files;
    FtpFileAdapter adapter;
    RecyclerView rv;
    private Activity activity;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    OnBackPressedCallback callback;

    public FilesFragment(){

    }

    public static FilesFragment newInstance(String dir){
        FilesFragment nestedList=new FilesFragment();
        Bundle args=new Bundle();
        args.putString("dir",dir);
        nestedList.setArguments(args);
        return nestedList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        files=new ArrayList<>();
        callback=new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(getArguments()!=null){
            dir=getArguments().getString("dir");
        }
        return inflater.inflate(R.layout.fragment_files, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        activity=getActivity();
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            // Handle the selected file URI
                            handleSelectedFileUri(uri);
                        }
                    }
                }
        );
        super.onViewCreated(view, savedInstanceState);
        String token=FtpLoginHandler.getToken(getActivity());
        UserRepository repository=new UserRepository();
        VMFactory factory=new VMFactory(repository,token);
        assert getActivity() != null;
        model=new ViewModelProvider(getActivity(),factory).get(VModel.class);
        rv=getView().findViewById(R.id.rv);
        adapter=new FtpFileAdapter(files,this);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setAdapter(adapter);
        FloatingActionButton fab=getView().findViewById(R.id.mkdir_btn);
        FloatingActionButton fab2=getView().findViewById(R.id.upload_btn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(activity);
                builder.setTitle("Создать папку");
                builder.setMessage("Имя:");
                final EditText input = new EditText(getActivity());
                builder.setView(input);
                builder.setPositiveButton("Создать", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString();
                        mkDir(name);
                    }
                });
                builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*"); // You can specify a more specific MIME type like "image/*" or "application/pdf"
                filePickerLauncher.launch(intent);
            }
        });
        model.getData().observe(getActivity(),data->{
            ftpLogin=data.getCredentials_for_ftp();
            new ListFilesTask().executeTask(dir,ftpLogin);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        callback.setEnabled(!Objects.equals(dir, "/"));
        MainFileActivity mfa=(MainFileActivity) getActivity();
        assert mfa != null;
        mfa.callback.setEnabled(Objects.equals(dir, "/"));
    }

    @Override
    public void onItemClick(FtpFileItem item) {
        if (item.isDirectory()) {
            FragmentTransaction transaction=getParentFragmentManager().beginTransaction();
            FilesFragment ff=FilesFragment.newInstance(dir+item.getName()+"/");
            transaction.replace(R.id.fragCont,ff);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            FragmentTransaction transaction=getParentFragmentManager().beginTransaction();
            FileInfoFragment fif=FileInfoFragment.newInstance(item,dir,ftpLogin);
            transaction.replace(R.id.fragCont,fif);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    @Override
    public void onItemLongClick(FtpFileItem item) {
        this.onItemClick(item);
    }
    private class ListFilesTask {
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        protected List<FtpFileItem> doInBackground(String path,String[] creds) {
            assert creds!=null;
            List<FTPFile> ftpFiles = FtpHandler.executeFetch("fetch_dir",path,creds);
            List<FtpFileItem> items = new ArrayList<>();

            for (FTPFile file : ftpFiles) {
                if (!file.getName().equals(".")) {
                    items.add(new FtpFileItem(file));
                }
            }
            return items;
        }
        protected void executeTask(String dirCurrent, String[] creds){
            CountDownLatch latch=new CountDownLatch(1);
            executor.execute(() -> {
                // Perform long-running operation
                List<FtpFileItem> result = doInBackground(dirCurrent,creds);
                onPostExecute(result);
                latch.countDown();
            });
            try{
                latch.await();
            } catch (InterruptedException e) {
                System.err.println("Interrupted! Use the program on your fear and risk.");
            }
        }
        protected void onPostExecute(List<FtpFileItem> items) {
            files.clear();
            files.addAll(items);
            adapter.updateData(files);
            AppCompatActivity tmp=(AppCompatActivity) activity;
            assert tmp != null;
            Objects.requireNonNull(tmp.getSupportActionBar()).setTitle(dir);
        }
    }

    public void mkDir(String name){
        boolean success=FtpHandler.executeTask("mkdir",null,dir+name,ftpLogin);
        if(!success) {
            Toast.makeText(getActivity(),"Ошибка создания папки",Toast.LENGTH_SHORT).show();
        }else{
            FragmentTransaction transaction=getParentFragmentManager().beginTransaction();
            FilesFragment ff=FilesFragment.newInstance(dir+name+"/");
            transaction.replace(R.id.fragCont,ff);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
    private void handleSelectedFileUri(Uri uri) {
        String fileName = null;
        try {
            assert getActivity() != null;
            try (Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (fileName != null) {
            try {
                InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
                if (inputStream != null) {
                    File outputFile = new File(getActivity().getCacheDir(), fileName); // Or a more suitable location
                    FileOutputStream outputStream = new FileOutputStream(outputFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    inputStream.close();
                    long sizeInput=outputFile.length();
                    String request="{\"nick\":\""+ftpLogin[0]+"\",\"pars\":\"used,quota\"}";
                    String response=HttpHandler.executeTask(request,"get_data");
                    MainFileActivity.JsonQuotaUsed data=new Gson().fromJson(response, MainFileActivity.JsonQuotaUsed.class);
                    if(data.getQuota()-data.getUsed()>=sizeInput){
                        Toast.makeText(getActivity(),"Загрузка на облако началась",Toast.LENGTH_SHORT).show();
                        boolean success=FtpHandler.executeTask("upload",outputFile,dir+fileName,ftpLogin);
                        if(success){
                            Toast.makeText(getActivity(),"Загрузка успешна",Toast.LENGTH_SHORT).show();
                            long newUsed=data.getUsed()+sizeInput;
                            request="{\"nick\":\""+ftpLogin[0]+"\",\"used\":"+newUsed+"}";
                            HttpHandler.executeTask(request,"update_data");
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            FilesFragment ff = (FilesFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.fragCont);
                            if (ff != null) {
                                transaction.detach(ff);
                                transaction.commit();
                                FragmentTransaction transaction2 = getActivity().getSupportFragmentManager().beginTransaction();
                                transaction2.attach(ff).commit();
                            }
                        }else{
                            Toast.makeText(getActivity(),"Сбой загрузки",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getActivity(),"Недостаточно свободного места в облаке",Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}