package me.szumielxd.adminutilities.bungee.objects;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.adminutilities.bungee.AdminUtilitiesBungee;
import me.szumielxd.adminutilities.common.objects.CommonScheduler;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class BungeeScheduler implements CommonScheduler {
	
	
private final @NotNull AdminUtilitiesBungee plugin;
	
	
	public BungeeScheduler(@NotNull AdminUtilitiesBungee plugin) {
		this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
	}
	

	@Override
	public @NotNull BungeeExecutedTask runTask(@NotNull Runnable task) {
		return new BungeeExecutedTask(this.plugin.getProxy().getScheduler().runAsync(this.plugin, task));
	}

	@Override
	public @NotNull BungeeExecutedTask runTaskAsynchronously(@NotNull Runnable task) {
		return this.runTask(task);
	}

	@Override
	public @NotNull BungeeExecutedTask runTaskLater(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
		return new BungeeExecutedTask(this.plugin.getProxy().getScheduler().schedule(this.plugin, task, delay, unit));
	}

	@Override
	public @NotNull BungeeExecutedTask runTaskLaterAsynchronously(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
		return this.runTaskLater(task, delay, unit);
	}

	@Override
	public @NotNull BungeeExecutedTask runTaskTimer(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit) {
		return new BungeeExecutedTask(this.plugin.getProxy().getScheduler().schedule(this.plugin, task, delay, period, unit));
	}

	@Override
	public @NotNull BungeeExecutedTask runTaskTimerAsynchronously(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit) {
		return this.runTaskTimerAsynchronously(task, delay, period, unit);
	}

	@Override
	public void cancel(int id) {
		this.plugin.getProxy().getScheduler().cancel(id);
	}

	@Override
	public void cancelAll() {
		this.plugin.getProxy().getScheduler().cancel(this.plugin);
	}
	
	
	public final class BungeeExecutedTask implements ExecutedTask {

		
		private final @NotNull ScheduledTask task;
		
		
		public BungeeExecutedTask(@NotNull ScheduledTask task) {
			this.task = Objects.requireNonNull(task, "task cannot be null");
		}
		
		
		@Override
		public void cancel() {
			this.task.cancel();
		}

		@Override
		public boolean isSync() {
			return false;
		}

		@Override
		public int getId() {
			return this.task.getId();
		}
		
	}
	

}
