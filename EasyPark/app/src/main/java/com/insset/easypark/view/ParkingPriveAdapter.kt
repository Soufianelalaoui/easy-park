package com.insset.easypark.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.insset.easypark.databinding.ItemMparkingPriveBinding
import com.insset.easypark.model.*

val diffUtilsParkingPrive = object : DiffUtil.ItemCallback<ParkingPrive>() {
    override fun areItemsTheSame(oldItem: ParkingPrive, newItem: ParkingPrive): Boolean {
        return oldItem == newItem
    }
    override fun areContentsTheSame(oldItem: ParkingPrive, newItem: ParkingPrive): Boolean {
        return oldItem == newItem
    }
}

class MParkingPriveViewHolder(
    val binding: ItemMparkingPriveBinding,
    onUpdateClick : ( parkingPrive: ParkingPrive) -> Unit

) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var parkingPrive: ParkingPrive
    init {
        binding.modifButton.setOnClickListener{
            onUpdateClick(parkingPrive)
        }
    }

    fun bind(parkingPrive: ParkingPrive) {
        this.parkingPrive = parkingPrive
        binding.textViewAdresse.text = parkingPrive.adresse?.libelle
        binding.textViewDispo.text = if(parkingPrive.disponibilite)"oui" else "non"
        binding.textViewSecurise.text = if(parkingPrive.securise) "oui" else "non"
        binding.textViewInterieur.text = if(parkingPrive.interieur) "oui" else "non"
        binding.textViewTarif.text = parkingPrive.tarif.toString()
    }
}


class MParkingPriveAdapter(
    private val onUpdateClick: (parkingPrive: ParkingPrive) -> Unit) : ListAdapter<ParkingPrive, RecyclerView.ViewHolder>(
    diffUtilsParkingPrive){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MParkingPriveViewHolder(
            ItemMparkingPriveBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), onUpdateClick
        )


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        (holder as MParkingPriveViewHolder).bind(getItem(position) as ParkingPrive)

}


