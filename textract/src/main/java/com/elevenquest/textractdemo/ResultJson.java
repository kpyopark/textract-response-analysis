package com.elevenquest.textractdemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.Relationship;
import com.amazonaws.services.textract.model.DocumentMetadata;

public class ResultJson {
  public DocumentMetadata documentMetaData;
  public List<Block> blocks;

  public List<Block> getCells() {
    List<Block> rtn = new ArrayList<Block>();
    blocks.forEach(block -> {
      if (block.getBlockType().equals("CELL")) rtn.add(block); 
    });
    return rtn;
  }

  public List<Block> getCells(String tableId) {
    final List<Block> cells = new ArrayList<Block>();
    Block tableBlock = getBlock(tableId);
    tableBlock.getRelationships().forEach(relation-> {
      if(relation.getType().equals("CHILD")) {
        relation.getIds().forEach(id -> {
          cells.add(getBlock(id));
        });
      }
    });
    return cells;
  }

  public List<Block> getCells(Block tableBlock) {
    return getCells(tableBlock.getId());
  }

  public List<Block> getTableBlocks() {
    List<Block> rtn = new ArrayList<Block>();
    blocks.forEach(block -> {
      if (block.getBlockType().equals("TABLE")) rtn.add(block);
    });
    return rtn;
  }

  public String getText(String id) {
    for(Block block: blocks) {
      if(block.getId().equals(id)) return block.getText();
    }
    return "";
  }

  public Block getBlock(String id) {
    for(Block block: blocks) {
      if(block.getId().equals(id))
        return block;
    }
    return null;
  }

  public List<Block> getBlocks(List<String> ids) {
    List<Block> rtn = new ArrayList<Block>();
    ids.forEach(id -> {
      rtn.add(getBlock(id));
    });
    return rtn;
  }

  public String getTableId(Block cellblock) {
    for(Block block: blocks) {
      if(block.getBlockType().equals("TABLE") 
        && block.getRelationships() != null) {
        for(Relationship relationship: block.getRelationships()) {
          if(relationship.getIds().contains(cellblock.getId()))
            return block.getId();
        }
      }
    }
    return null;
  }

  public Block getLastColumn(String tableBlockId, Block activeCell) {
    Block lastCell = null;
    for(Block cell:getCells(tableBlockId)) {
      if (cell.getRowIndex() == activeCell.getRowIndex()) {
        if (lastCell == null) {
          lastCell = cell;
        }
        if (cell.getColumnIndex() > lastCell.getColumnIndex()) {
          lastCell = cell;
        }
      }
    }
    return lastCell;
  }

  public Block getLastColumn(Block activeCell) {
    return getLastColumn(getTableId(activeCell), activeCell);
  }

  public String getChildText(Block block) {
    final StringBuffer rtn = new StringBuffer();
    if (block.getRelationships() != null) 
      block.getRelationships().forEach(relation -> {
      if (relation.getType().equals("CHILD")) {
        relation.getIds().forEach(childId -> {
          rtn.append(" ").append(getText(childId));
        });
      }
    });
    return rtn.length() > 1 ? rtn.substring(1) : "";
  }

  public String getChildText(String id) {
    return getChildText(getBlock(id));
  }

  public List<Block> getKeyCells() {
    List<Block> rtn = new ArrayList<Block>();
    blocks.forEach(block -> {
      if(block.getBlockType().equals("KEY_VALUE_SET")
      && block.getEntityTypes().get(0).equals("KEY")
      ) rtn.add(block);
    });
    return rtn;
  }

  public String getKeyText(Block keyCell) {
    return getChildText(keyCell);
  }

  public String getValueText(Block keycell) {
    for(Relationship rel: keycell.getRelationships()) {
      if(rel.getType().equals("VALUE")) {
        return getChildText(rel.getIds().get(0));
      }
    }
    return null;
  }

}
