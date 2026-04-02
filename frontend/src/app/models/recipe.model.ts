export interface Recipe {
  name: string;
  time: string;
  difficulty: string;
  ingredients: string[];
  steps: string[];
}

export interface RecipeResponse {
  recipes: Recipe[];
}

export interface ApiErrorBody {
  error?: string;
}
