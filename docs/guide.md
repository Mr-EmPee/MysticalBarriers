## Creating a barrier
To create a barrier you first need to **select the area of your barrier**, in order
to that you need you a barrier wand

```
/mb wand
```

Use your barrier wand by right-clicking on a block to select the two corners of your barrier.
After having selected the corners execute

```
/mb create <barrier-name>
```

replace `<barrier-name>` with a name of your choice. To test it try to set the permission `mysticalbarriers.bypass.<barrier-name>` to `false`

## Managing a barrier
To access the management GUI of a barrier you need to execute

```
/mb edit <barrier-name>
```

from there you can:
- Modify the barrier material/structure
- Modify the barrier visibility range
- Delete the barrier

## Structure barrier wall

By default the barrier is composed only by 1 type of block, but it's possible to make it uses
your own build instead.

- Create a barrier / If you already have a barrier ignore this step
- Inside the barrier area **place bedrock** blocks to refine the selection (This allows you to create more complex barrier region)
- Go into the editing GUI of the barrier and **right-click** on "Change Material"
- If the bedrock disappears it means that you successfully refined the barrier area
- Build the barrier structure inside the newly refined area (Were you placed the bedrock blocks)
- Go into the editing GUI of the barrier and **left-click** on "Update structure"
- If the structure disappears it means that you have updated the barrier.