import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ApiErrorBody, RecipeResponse } from '../models/recipe.model';

@Injectable({
  providedIn: 'root',
})
export class RecipeService {
  private readonly http = inject(HttpClient);

  suggestRecipes(ingredients: string[]): Observable<RecipeResponse> {
    const url = `${environment.apiUrl}/api/recipes/suggest`;
    return this.http.post<RecipeResponse>(url, { ingredients }).pipe(
      catchError((err: HttpErrorResponse) => {
        const body = err.error as ApiErrorBody | string | null;
        const message =
          typeof body === 'object' && body !== null && typeof body.error === 'string'
            ? body.error
            : err.message || 'Nie udało się pobrać przepisów.';
        return throwError(() => new Error(message));
      }),
    );
  }
}
