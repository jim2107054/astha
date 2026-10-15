'use client';

import React, { useState } from 'react';

export interface GraphNode {
  id: string;
  label: string;
  type: 'ACCOUNT' | 'DEVICE' | 'AGENT' | 'MERCHANT';
  riskScore: number;
  hopDistance: number;
}

export interface GraphEdge {
  source: string;
  target: string;
  weight: number;
  label: string;
  isHighRisk?: boolean;
}

export interface GraphCanvasProps {
  nodes: GraphNode[];
  edges: GraphEdge[];
  selectedNodeId?: string;
  onSelectNode?: (nodeId: string) => void;
  width?: number;
  height?: number;
}

export function GraphCanvas({
  nodes,
  edges,
  selectedNodeId,
  onSelectNode,
  width = 720,
  height = 420,
}: GraphCanvasProps) {
  const [activeNode, setActiveNode] = useState<string | undefined>(selectedNodeId || nodes[0]?.id);

  const centerX = width / 2;
  const centerY = height / 2;
  const ring1Radius = 110;
  const ring2Radius = 180;

  // Position nodes radially around the center (seed) node
  const seedNode = nodes[0];
  const hop1Nodes = nodes.filter((n) => n.hopDistance === 1);
  const hop2Nodes = nodes.filter((n) => n.hopDistance >= 2);

  const nodePositions = new Map<string, { x: number; y: number }>();

  if (seedNode) {
    nodePositions.set(seedNode.id, { x: centerX, y: centerY });
  }

  hop1Nodes.forEach((node, idx) => {
    const angle = (2 * Math.PI * idx) / Math.max(1, hop1Nodes.length) - Math.PI / 2;
    nodePositions.set(node.id, {
      x: centerX + ring1Radius * Math.cos(angle),
      y: centerY + ring1Radius * Math.sin(angle),
    });
  });

  hop2Nodes.forEach((node, idx) => {
    const angle = (2 * Math.PI * idx) / Math.max(1, hop2Nodes.length) - Math.PI / 4;
    nodePositions.set(node.id, {
      x: centerX + ring2Radius * Math.cos(angle),
      y: centerY + ring2Radius * Math.sin(angle),
    });
  });

  const selectedNodeObj = nodes.find((n) => n.id === activeNode);

  return (
    <div className="relative rounded-xl border border-slate-800 bg-slate-950 p-4 overflow-hidden select-none">
      <div className="absolute top-4 left-4 z-10 flex items-center gap-3 text-xs">
        <span className="flex items-center gap-1.5 text-slate-300">
          <span className="w-2.5 h-2.5 rounded-full bg-red-500"></span> Mule Ring (Critical)
        </span>
        <span className="flex items-center gap-1.5 text-slate-300">
          <span className="w-2.5 h-2.5 rounded-full bg-amber-500"></span> Shared Device
        </span>
        <span className="flex items-center gap-1.5 text-slate-300">
          <span className="w-2.5 h-2.5 rounded-full bg-teal-500"></span> Legitimate Counterparty
        </span>
      </div>

      <svg width="100%" height={height} viewBox={`0 0 ${width} ${height}`} className="w-full h-auto">
        {/* Topology background rings */}
        <circle cx={centerX} cy={centerY} r={ring1Radius} fill="none" stroke="#1e293b" strokeDasharray="4,4" />
        <circle cx={centerX} cy={centerY} r={ring2Radius} fill="none" stroke="#1e293b" strokeDasharray="4,4" />

        {/* Edges */}
        {edges.map((edge, i) => {
          const src = nodePositions.get(edge.source);
          const tgt = nodePositions.get(edge.target);
          if (!src || !tgt) return null;

          const isCritical = edge.isHighRisk;

          return (
            <g key={`edge-${i}`}>
              <line
                x1={src.x}
                y1={src.y}
                x2={tgt.x}
                y2={tgt.y}
                stroke={isCritical ? '#ef4444' : '#334155'}
                strokeWidth={isCritical ? 2.5 : 1.2}
                strokeDasharray={isCritical ? 'none' : '2,2'}
                opacity={0.8}
              />
              <text
                x={(src.x + tgt.x) / 2}
                y={(src.y + tgt.y) / 2 - 4}
                fill="#94a3b8"
                fontSize={9}
                textAnchor="middle"
                className="font-mono"
              >
                {edge.label}
              </text>
            </g>
          );
        })}

        {/* Nodes */}
        {nodes.map((node) => {
          const pos = nodePositions.get(node.id);
          if (!pos) return null;

          const isSelected = activeNode === node.id;
          const isCritical = node.riskScore >= 80;
          const isMedium = node.riskScore >= 40 && node.riskScore < 80;

          const fillColor = isCritical ? '#ef4444' : isMedium ? '#f59e0b' : '#0d9488';

          return (
            <g
              key={node.id}
              onClick={() => {
                setActiveNode(node.id);
                if (onSelectNode) onSelectNode(node.id);
              }}
              className="cursor-pointer"
            >
              {isSelected && (
                <circle cx={pos.x} cy={pos.y} r={24} fill="none" stroke="#38bdf8" strokeWidth={2} strokeDasharray="3,3" />
              )}
              <circle
                cx={pos.x}
                cy={pos.y}
                r={16}
                fill={fillColor}
                stroke="#0f172a"
                strokeWidth={3}
                className="transition-transform hover:scale-110"
              />
              <text
                x={pos.x}
                y={pos.y + 4}
                fill="#ffffff"
                fontSize={10}
                fontWeight="bold"
                textAnchor="middle"
              >
                {node.type[0]}
              </text>
              <text
                x={pos.x}
                y={pos.y + 28}
                fill="#cbd5e1"
                fontSize={10}
                fontWeight={isSelected ? 'bold' : 'normal'}
                textAnchor="middle"
                className="font-mono"
              >
                {node.label}
              </text>
            </g>
          );
        })}
      </svg>

      {/* Node Inspection Drawer */}
      {selectedNodeObj && (
        <div className="absolute bottom-4 right-4 z-10 w-64 rounded-lg border border-slate-800 bg-slate-900/90 p-3 shadow-xl backdrop-blur text-xs">
          <div className="flex items-center justify-between pb-1 mb-2 border-b border-slate-800">
            <span className="font-bold text-white font-mono">{selectedNodeObj.id}</span>
            <span className="px-1.5 py-0.2 rounded text-[10px] font-bold bg-slate-800 text-teal-400">
              Hop {selectedNodeObj.hopDistance}
            </span>
          </div>
          <div className="space-y-1 text-slate-300">
            <div>Type: <strong className="text-white">{selectedNodeObj.type}</strong></div>
            <div>
              Risk Score:{' '}
              <strong className={selectedNodeObj.riskScore >= 80 ? 'text-red-400 font-bold' : 'text-teal-400'}>
                {selectedNodeObj.riskScore} / 100
              </strong>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
