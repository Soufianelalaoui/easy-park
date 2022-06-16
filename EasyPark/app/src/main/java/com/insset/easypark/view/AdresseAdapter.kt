package com.insset.easypark.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.insset.easypark.databinding.ItemAdresseBinding
import com.insset.easypark.model.Adresse

private var diffUtilis = object : DiffUtil.ItemCallback<Adresse>() {
    override fun areItemsTheSame(oldItem: Adresse, newItem: Adresse): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Adresse, newItem: Adresse): Boolean {
        return oldItem == newItem
    }

}

class AdresseViewHolder(val binding: ItemAdresseBinding,
                        onNavigationClick : ( recyclerViewObject: Adresse) -> Unit,
                        onDeleteClick: (address: Adresse) -> Unit) : RecyclerView.ViewHolder(binding.root) {
    private lateinit var ui: Adresse
    init {

        binding.floatingActionButtonSearch.setOnClickListener{ onNavigationClick(ui) }
        binding.floatingActionButtonDelete.setOnClickListener { onDeleteClick(ui) }
    }
    fun bind(adresse: Adresse) {
        ui = adresse
        binding.libelle.text = adresse.libelle
        binding.alias.text = adresse.alias
    }
}

class AdresseAdapter(
    private val onNavigationClick: (address: Adresse) -> Unit,
    private val onDeleteClick: (address: Adresse) -> Unit) : ListAdapter<Adresse ,AdresseViewHolder>(diffUtilis) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AdresseViewHolder(
            ItemAdresseBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), onNavigationClick, onDeleteClick
        )


    override fun onBindViewHolder(holder: AdresseViewHolder, position: Int) =
        holder.bind(getItem(position))

}