package com.facens.mobile_ac2;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText edtTitulo, edtAutor, edtAnopubli, edtBusca;
    private Spinner spinnerGenero, spinnerStatus, spinnerFiltro;
    private CheckBox chkFavorito, chkFiltroFavoritos;
    private Button btnSalvar;
    private RecyclerView recyclerLivros;
    private LivrosAdapter adapter;
    private List<Livros> listaLivros = new ArrayList<>();
    private Livros livrosEditando = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        edtTitulo = findViewById(R.id.edtTitulo);
        edtAutor = findViewById(R.id.edtAutor);
        edtAnopubli = findViewById(R.id.edtAnopubli);
        edtBusca = findViewById(R.id.edtBusca);
        spinnerGenero = findViewById(R.id.spinnerGenero);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerFiltro = findViewById(R.id.spinnerFiltro);
        chkFavorito = findViewById(R.id.chkFavorito);
        chkFiltroFavoritos = findViewById(R.id.chkFiltroFavoritos);
        btnSalvar = findViewById(R.id.btnSalvar);
        recyclerLivros = findViewById(R.id.recyclerLivros);

        configurarRecycler();
        configurarSpinners();
        carregarLivros();

        btnSalvar.setOnClickListener(v -> salvarLivro());

        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                carregarLivros();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        chkFiltroFavoritos.setOnCheckedChangeListener((buttonView, isChecked) -> carregarLivros());

        edtBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                carregarLivros();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void configurarRecycler() {
        adapter = new LivrosAdapter(listaLivros);
        recyclerLivros.setLayoutManager(new LinearLayoutManager(this));
        recyclerLivros.setAdapter(adapter);

        // Clique curto para editar
        adapter.setOnItemClickListener(livro -> {
            livrosEditando = livro;
            edtTitulo.setText(livro.getTitulo());
            edtAutor.setText(livro.getAutor());
            edtAnopubli.setText(String.valueOf(livro.getAno()));
            chkFavorito.setChecked(livro.isFavorito());

            ArrayAdapter<String> genAdapter = (ArrayAdapter<String>) spinnerGenero.getAdapter();
            spinnerGenero.setSelection(genAdapter.getPosition(livro.getGenero()));

            ArrayAdapter<String> statusAdapter = (ArrayAdapter<String>) spinnerStatus.getAdapter();
            spinnerStatus.setSelection(statusAdapter.getPosition(livro.getStatus()));

            btnSalvar.setText("Atualizar Livro");
        });

        // Clique longo para excluir
        adapter.setOnItemLongClickListener(this::confirmarExclusao);
    }

    private void configurarSpinners() {
        String[] generos = {"Romance", "Fantasia", "Terror", "Ficção Científica", "Biografia"};
        ArrayAdapter<String> adapterGen = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, generos);
        adapterGen.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenero.setAdapter(adapterGen);

        String[] status = {"Quero ler", "Lendo", "Concluído"};
        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, status);
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapterStatus);

        String[] filtros = {"Todos", "Quero ler", "Lendo", "Concluído"};
        ArrayAdapter<String> adapterFiltro = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filtros);
        adapterFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltro.setAdapter(adapterFiltro);
    }

    private void salvarLivro() {
        String titulo = edtTitulo.getText().toString().trim();
        String autor = edtAutor.getText().toString().trim();
        String anoStr = edtAnopubli.getText().toString().trim();

        if (titulo.isEmpty() || autor.isEmpty() || anoStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int ano;
        try {
            ano = Integer.parseInt(anoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ano inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        String genero = spinnerGenero.getSelectedItem().toString();
        String status = spinnerStatus.getSelectedItem().toString();
        boolean favorito = chkFavorito.isChecked();

        if (livrosEditando == null) {
            // Criar novo livro
            Livros novoLivro = new Livros(null, titulo, autor, ano, genero, status, favorito);
            db.collection("livros")
                    .add(novoLivro)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "Livro salvo com sucesso!", Toast.LENGTH_SHORT).show();
                        limparCampos();
                        carregarLivros();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar livro", Toast.LENGTH_SHORT).show());
        } else {
            // Atualizar livro existente
            livrosEditando.setTitulo(titulo);
            livrosEditando.setAutor(autor);
            livrosEditando.setAno(ano);
            livrosEditando.setGenero(genero);
            livrosEditando.setStatus(status);
            livrosEditando.setFavorito(favorito);

            db.collection("livros").document(livrosEditando.getId())
                    .set(livrosEditando)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Livro atualizado!", Toast.LENGTH_SHORT).show();
                        limparCampos();
                        carregarLivros();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Erro ao atualizar", Toast.LENGTH_SHORT).show());
        }
    }

    private void carregarLivros() {
        Query query = db.collection("livros");

        String filtro = spinnerFiltro.getSelectedItem().toString();
        if (!filtro.equals("Todos")) {
            query = query.whereEqualTo("status", filtro);
        }

        if (chkFiltroFavoritos.isChecked()) {
            query = query.whereEqualTo("favorito", true);
        }

        query.get().addOnSuccessListener(snapshots -> {
            listaLivros.clear();
            String busca = edtBusca.getText().toString().toLowerCase().trim();
            for (QueryDocumentSnapshot doc : snapshots) {
                Livros livro = doc.toObject(Livros.class);
                livro.setId(doc.getId());
                
                if (busca.isEmpty() || livro.getTitulo().toLowerCase().contains(busca)) {
                    listaLivros.add(livro);
                }
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> Toast.makeText(this, "Erro ao carregar livros", Toast.LENGTH_SHORT).show());
    }

    private void confirmarExclusao(Livros livro) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Livro")
                .setMessage("Deseja realmente excluir o livro \"" + livro.getTitulo() + "\"?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    db.collection("livros").document(livro.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(MainActivity.this, "Livro excluído!", Toast.LENGTH_SHORT).show();
                                carregarLivros();

                                if (livrosEditando != null && livrosEditando.getId().equals(livro.getId())) {
                                    limparCampos();
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Erro ao excluir", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void limparCampos() {
        edtTitulo.setText("");
        edtAutor.setText("");
        edtAnopubli.setText("");
        chkFavorito.setChecked(false);
        spinnerGenero.setSelection(0);
        spinnerStatus.setSelection(0);
        livrosEditando = null;
        btnSalvar.setText("Salvar Livro");
    }
}
