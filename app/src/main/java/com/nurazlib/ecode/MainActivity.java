package com.nurazlib.ecode;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.apache.commons.net.ftp.FTPClient;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {

    private EditText codeEditor;
    private FTPClient ftpClient;
    private ChannelSftp sftpChannel;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize the code editor
        codeEditor = findViewById(R.id.code_editor);

        // Set up buttons
        Button buttonChooseFile = findViewById(R.id.button_choose_file);
        buttonChooseFile.setOnClickListener(v -> chooseFile());

        Button buttonConnectFTP = findViewById(R.id.button_connect_ftp);
        buttonConnectFTP.setOnClickListener(v -> showFtpDialog());

        Button buttonConnectSFTP = findViewById(R.id.button_connect_sftp);
        buttonConnectSFTP.setOnClickListener(v -> showSftpDialog());

        // Set a simple TextWatcher to show a toast on text change
        codeEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show toast when text is changed
                Toast.makeText(MainActivity.this, "Text changed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed here
            }
        });
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            try {
                filePath = data.getData().getPath();
                FileInputStream inputStream = new FileInputStream(new File(filePath));
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                codeEditor.setText(new String(bytes));
                inputStream.close();
            } catch (Exception e) {
                Toast.makeText(this, "File load failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showFtpDialog() {
        LayoutInflater inflater = getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("FTP Details");
        builder.setView(inflater.inflate(R.layout.dialog_ftp_details, null));
        builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                AlertDialog alertDialog = (AlertDialog) dialog;
                String host = ((EditText) alertDialog.findViewById(R.id.editTextFtpHost)).getText().toString();
                int port = Integer.parseInt(((EditText) alertDialog.findViewById(R.id.editTextFtpPort)).getText().toString());
                String username = ((EditText) alertDialog.findViewById(R.id.editTextFtpUsername)).getText().toString();
                String password = ((EditText) alertDialog.findViewById(R.id.editTextFtpPassword)).getText().toString();
                connectFTP(host, port, username, password);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showSftpDialog() {
        LayoutInflater inflater = getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SFTP Details");
        builder.setView(inflater.inflate(R.layout.dialog_sftp_details, null));
        builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                AlertDialog alertDialog = (AlertDialog) dialog;
                String host = ((EditText) alertDialog.findViewById(R.id.editTextSftpHost)).getText().toString();
                int port = Integer.parseInt(((EditText) alertDialog.findViewById(R.id.editTextSftpPort)).getText().toString());
                String username = ((EditText) alertDialog.findViewById(R.id.editTextSftpUsername)).getText().toString();
                String password = ((EditText) alertDialog.findViewById(R.id.editTextSftpPassword)).getText().toString();
                connectSFTP(host, port, username, password);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void connectFTP(String host, int port, String username, String password) {
        new Thread(() -> {
            try {
                ftpClient = new FTPClient();
                ftpClient.connect(host, port);
                ftpClient.login(username, password);
                ftpClient.enterLocalPassiveMode();

                InputStream inputStream = ftpClient.retrieveFileStream("/path/to/file.txt");
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                runOnUiThread(() -> codeEditor.setText(new String(bytes)));
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "FTP connection failed", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void connectSFTP(String host, int port, String username, String password) {
        new Thread(() -> {
            try {
                JSch jsch = new JSch();
                Session session = jsch.getSession(username, host, port);
                session.setPassword(password);

                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);

                session.connect();
                sftpChannel = (ChannelSftp) session.openChannel("sftp");
                sftpChannel.connect();

                InputStream inputStream = sftpChannel.get("/path/to/file.txt");
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                runOnUiThread(() -> codeEditor.setText(new String(bytes)));
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "SFTP connection failed", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}