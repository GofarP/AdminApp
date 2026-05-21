package com.gopro.AdminApp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gopro.AdminApp.ui.theme.AdminAppTheme
import com.gopro.AdminApp.ui.theme.components.ButtonColorType
import com.gopro.AdminApp.ui.theme.components.CustomButton

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalculatorScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}


@Composable
fun CalculatorScreen(modifier: Modifier= Modifier){
    var number1 by remember { mutableStateOf("") }
    var number2 by remember { mutableStateOf("") }
    var choosenOperation by remember { mutableStateOf("Tambah") }
    var choosenMode by remember { mutableStateOf("Calculator") }

    var hasilText by remember { mutableStateOf("Hasil:0") }

    var listOperation=listOf("Tambah", "Kurang","Kali","Bagi")
    var listMode=listOf("Concat", "Calculator")

    val context=LocalContext.current;

    Column(
        modifier=modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,

    ) {
        Text(
            text="Kalkulator Keren",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = number1,
            onValueChange = { number1 = it },
            label = { Text(if (choosenMode == "Calculator") "Angka Pertama" else "Kata Pertama") },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (choosenMode == "Calculator") KeyboardType.Number else KeyboardType.Text
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = number2,
            onValueChange = { number2 = it },
            label = { Text(if (choosenMode == "Calculator") "Angka Kedua" else "Kata Kedua") },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (choosenMode == "Calculator") KeyboardType.Number else KeyboardType.Text
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Pilih Mode:",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        listMode.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        choosenMode = mode
                        number1 = ""
                        number2 = ""
                        hasilText = "Hasil:0"
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (choosenMode == mode),
                    onClick = {
                        choosenMode = mode
                        number1=""
                        number2=""
                        hasilText="Hasil:0"
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = mode)
            }
        }

        if(choosenMode=="Calculator"){
            Spacer(modifier= Modifier.height(16.dp))
            Text(
                text = "Pilih Operasi:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            listOperation.forEach { operasi ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { choosenOperation = operasi }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (choosenOperation == operasi),
                        onClick = { choosenOperation = operasi }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = operasi)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        CustomButton(
            text = if (choosenMode == "Concat") "Gabungkan Kata" else "Hitung Angka",
            colorType = if (choosenMode == "Concat") ButtonColorType.PRIMARY else ButtonColorType.SUCCESS,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if(number1.isEmpty() || number2.isEmpty()){
                    Toast.makeText(context, "Silahkan isi bidang yang masih kosong", Toast.LENGTH_SHORT).show()
                }

                if (choosenMode == "Concat") {
                    hasilText = "Hasil: $number1 $number2"
                } else {
                    val num1 = number1.toDoubleOrNull() ?: 0.0
                    val num2 = number2.toDoubleOrNull() ?: 0.0

                    // Gunakan tipe Any agar bisa menampung Double atau String ("Tidak bisa bagi 0")
                    val kalkulasi: Any = when (choosenOperation) {
                        "Tambah" -> num1 + num2
                        "Kurang" -> num1 - num2
                        "Kali" -> num1 * num2
                        "Bagi" -> if (num2 != 0.0) num1 / num2 else "Tidak bisa bagi 0"
                        else -> 0.0
                    }

                    // Validasi tampilan agar aman dari crash matematika
                    hasilText = if (kalkulasi is Double) {
                        if (kalkulasi % 1 == 0.0) {
                            "Hasil: ${kalkulasi.toInt()}"
                        } else {
                            "Hasil: $kalkulasi"
                        }
                    } else {
                        "Hasil: $kalkulasi"
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = hasilText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun KalkulatorScreenPreview() {
    AdminAppTheme {
        CalculatorScreen()
    }
}


