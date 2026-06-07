package com.resofy.music.adapter

import ServerConfigEntity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.resofy.music.R

class ServerConfigAdapter(
    private var servers: List<ServerConfigEntity>,
    private var activeServerId: Int,
    private val onSelect: (ServerConfigEntity) -> Unit,
    private val onSync: (ServerConfigEntity) -> Unit,
    private val onEdit: (ServerConfigEntity) -> Unit,
    private val onDelete: (ServerConfigEntity) -> Unit
) : RecyclerView.Adapter<ServerConfigAdapter.ViewHolder>() {

    fun updateServers(newServers: List<ServerConfigEntity>, activeId: Int) {
        servers = newServers
        activeServerId = activeId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_server_config, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(servers[position])
    }

    override fun getItemCount() = servers.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tvServerName)
        private val tvUrl: TextView = view.findViewById(R.id.tvServerUrl)
        private val btnSync: ImageButton = view.findViewById(R.id.btnSync)
        private val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        private val activeIndicator: View = view.findViewById(R.id.activeIndicator)

        fun bind(server: ServerConfigEntity) {
            tvName.text = server.name.ifEmpty { server.url }
            tvUrl.text = server.url
            activeIndicator.visibility = if (server.id == activeServerId) View.VISIBLE else View.INVISIBLE
            itemView.setOnClickListener { onSelect(server) }
            btnSync.setOnClickListener { onSync(server) }
            btnEdit.setOnClickListener { onEdit(server) }
            btnDelete.setOnClickListener { onDelete(server) }
        }
    }
}