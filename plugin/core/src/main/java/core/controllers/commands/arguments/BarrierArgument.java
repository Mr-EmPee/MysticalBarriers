package core.controllers.commands.arguments;

import com.github.empee.commands.CommandContext;
import com.github.empee.commands.arguments.Argument;
import com.github.empee.commands.exceptions.ArgumentException;
import com.github.empee.commands.suggestions.CommandSuggestion;
import core.MysticalBarriers;
import core.model.Barrier;
import core.services.BarriersService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class BarrierArgument implements Argument<Barrier> {

  public static final String ERROR_NOT_FOUND = "barrier_not_found";

  private final String id;
  private final BarriersService barriersService = MysticalBarriers.getInstance(BarriersService.class);

  private Function<CommandContext<?>, ?> executor;
  private final Function<CommandContext<?>, List<CommandSuggestion>> suggestions = suggestionProvider();

  private @NotNull Function<CommandContext<?>, List<CommandSuggestion>> suggestionProvider() {
    return ctx -> barriersService.findAll().stream()
        .map(b -> CommandSuggestion.of(b.getId(), null))
        .collect(Collectors.toList());
  }

  public String getParser() {
    return "brigadier:string";
  }

  @Override
  public @NotNull Barrier parse(CommandContext<?> context, String input) {
    return barriersService.findById(input).orElseThrow(
        () -> ArgumentException.parsing(this, input, ERROR_NOT_FOUND)
    );
  }

  public BarrierArgument withExecutor(Function<CommandContext<?>, ?> executor) {
    this.executor = executor;
    return this;
  }

  public BarrierArgument withExecutor(Consumer<CommandContext<?>> executor) {
    return withExecutor((ctx) -> {
      executor.accept(ctx);
      return null;
    });
  }

}
