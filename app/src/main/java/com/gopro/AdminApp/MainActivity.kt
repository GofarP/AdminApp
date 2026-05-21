package com.gopro.AdminApp

import android.R
import android.os.Bundle
import android.widget.Space
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

    var hasilText by remember { mutableStateOf("Hasil: 0") }

    var listOperation=listOf("Tambah","Kurang","Kali","Bagi")

    Column(
        modifier=modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text="Kalkulator Keren",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier= Modifier.height(24.dp))

        OutlinedTextField(
            value = number1,
            onValueChange = {number1=it},
            label={Text("Angka Pertama")},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier= Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value=number2,
            onValueChange = {number2=it},
            label={Text("Angka Kedua")},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier= Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text="Pilih Operasi:",
            fontWeight = FontWeight.Medium,
            modifier= Modifier.align (Alignment.Start)
        )

        listOperation.forEach { operasi->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { choosenOperation = operasi } // Bisa diklik pada barisnya
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

        Spacer(modifier= Modifier.height(24.dp))

        Button(
            onClick ={
                val num1 = number1.toDoubleOrNull() ?: 0.0
                val num2 = number2.toDoubleOrNull() ?: 0.0

                val kalkulasi=when(choosenOperation){
                    "Tambah" -> num1 + num2
                    "Kurang" -> num1 - num2
                    "Kali" -> num1 * num2
                    "Bagi" -> if (num2 != 0.0) num1 / num2 else "Tidak bisa bagi 0"
                    else -> 0.0
                }
                hasilText = "Hasil: $kalkulasi"
            },
            modifier=Modifier.fillMaxWidth()
        ) {
            Text("Hitung")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text=hasilText,
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


