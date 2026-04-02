import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Recipe } from './models/recipe.model';
import { RecipeService } from './services/recipe.service';

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  private readonly recipeService = inject(RecipeService);

  title = 'QuickBite';
  ingredientsText = '';

  readonly recipes = signal<Recipe[] | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  submit(): void {
    const ingredients = this.parseIngredients(this.ingredientsText);
    if (ingredients.length === 0) {
      this.error.set('Podaj co najmniej jeden składnik (oddziel przecinkiem lub nową linią).');
      this.recipes.set(null);
      return;
    }

    this.error.set(null);
    this.loading.set(true);
    this.recipes.set(null);

    this.recipeService.suggestRecipes(ingredients).subscribe({
      next: (res) => {
        this.recipes.set(res.recipes);
        this.loading.set(false);
      },
      error: (err: Error) => {
        this.error.set(err.message);
        this.loading.set(false);
      },
    });
  }

  private parseIngredients(text: string): string[] {
    return text
      .split(/[\n,;]+/)
      .map((s) => s.trim())
      .filter((s) => s.length > 0);
  }
}
